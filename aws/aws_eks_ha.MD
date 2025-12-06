Nice — I’ll walk you through this end-to-end and practically: what **Multi-AZ node groups** are, how to **configure** them (concrete `eksctl` / CloudFormation / Terraform examples), how **EKS behaves across AZs**, how to design **multi-region** EKS (active-active / DR) and the tools + operators you’ll use to manage it in production. I’ll include config snippets, Kubernetes scheduling best practices for HA, and the recommended operational components (autoscalers, node lifecycle handlers, GitOps, etc.).

Where helpful I’ll point to AWS docs and AWS blog posts so you can follow exact references. ([AWS Documentation][1])

---

# 1 — Core concepts (short, mental model)

* **Control plane (masters)** — managed by AWS for EKS: multi-AZ, highly available (you don’t run/control etcd). AWS guarantees control-plane HA. ([AWS Documentation][2])
* **Worker nodes (node groups)** — your EC2 instances (managed node groups, self-managed, or provisioned via Karpenter). These must be spread across AZs to achieve availability. ([AWS Documentation][3])
* **Multi-AZ node group** — a node group whose Auto Scaling Group (ASG) launches instances into **multiple subnets / AZs**. If AZ-A fails, nodes in AZ-B/C keep container workloads running (pods get rescheduled). ([AWS Documentation][4])
* **Multi-region** EKS — separate EKS clusters in different AWS Regions. Combine with global routing (Route53 latency/geo or AWS Global Accelerator) and multi-region data replication (DynamoDB Global Tables, Aurora Global DB) for active-active. ([Amazon Web Services, Inc.][5])

---

# 2 — Multi-AZ node groups: why and how they work

## Why multi-AZ node groups?

* Avoid single-AZ failure (AZ outage = worker nodes in that AZ gone).
* Better resource availability and fault tolerance.
* ALB/Ingress will route to healthy nodes across AZs; Kubernetes rescheduler recreates pods on surviving AZs. ([AWS Documentation][2])

## What AWS/EKS components are involved

* **Managed Node Group** (EKS) → each node group creates an Auto Scaling Group with instances in the subnet(s) you provide. If you provide multiple private subnets across AZs, ASG will span those AZs. Managed node groups simplify lifecycle (rolling upgrades). ([AWS Documentation][1])
* **Kube-scheduler** + **kube-proxy** handle traffic inside cluster; ALB/NLB target groups register node IPs (nodeports) from each node across AZs. ([AWS Documentation][2])

---

# 3 — Example: create a multi-AZ managed node group with `eksctl`

`eksctl` is the simplest tool for dev/prod clusters. Below is a minimal cluster + managed nodegroup config (3 AZs):

```yaml
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig
metadata:
  name: prod-cluster
  region: us-east-1
vpc:
  subnets:
    public:
      us-east-1a: { id: subnet-aaa }
      us-east-1b: { id: subnet-bbb }
      us-east-1c: { id: subnet-ccc }
nodeGroups:
  - name: ng-general
    instanceType: t3.medium
    desiredCapacity: 3
    minSize: 3
    maxSize: 6
    iam:
      withAddonPolicies:
        autoScaler: true
    ssh:
      allow: true
    # If you specify the three subnets above, eksctl/ASG will spread instances across the AZs
```

Create it with:

```bash
eksctl create cluster -f cluster-config.yaml
# or, create only nodegroup for existing cluster:
eksctl create nodegroup --cluster prod-cluster --config-file nodegroup.yaml
```

Notes:

* `desiredCapacity: 3` will typically create one node per AZ (if 3 AZs configured). You can scale out for capacity. ([Repost][6])

---

# 4 — What to configure in AWS (VPC/Subnets/ASG/Node IAM)

* **VPC**: create private subnets in **≥2** AZs (preferably 3) and put nodegroups into those private subnets.
* **Security groups**: nodes need SGs for API, pod traffic, and control plane comms.
* **ASG**: the managed node group creates ASG with instance distribution across given subnets (AZs). You can configure mixed instance types or spot instances for cost optimization. ([Medium][7])

---

# 5 — Node diversification & resilience patterns

* **Multiple node groups**: e.g., `ng-general-ondemand`, `ng-batch-spot`, `ng-gpu` — each can span AZs and use different instance types / taints. This helps tolerate capacity shortages and provides workload isolation. ([Medium][7])
* **Spot + On-Demand mix**: run stateless workloads on spot groups (with fallback to on-demand). Use Pod priority/NodeSelector/taints to control placement.
* **Karpenter**: for dynamic provisioning (fast scale up with flexible instance selection) — good for variable workloads. ([AWS Documentation][8])

---

# 6 — Kubernetes scheduling & anti-affinity best practices (avoid blast radius)

To keep pods spread across AZs, use:

### Pod anti-affinity (example)

```yaml
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
    - labelSelector:
        matchExpressions:
        - key: app
          operator: In
          values:
          - payments
      topologyKey: topology.kubernetes.io/zone
```

### TopologySpreadConstraints (example)

```yaml
topologySpreadConstraints:
- maxSkew: 1
  topologyKey: topology.kubernetes.io/zone
  whenUnsatisfiable: DoNotSchedule
  labelSelector:
    matchLabels:
      app: payments
```

### PodDisruptionBudget (PDB)

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: payments-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: payments
```

These ensure pods get rebalanced across AZs and survive node drains/upgrades. ([AWS Documentation][2])

---

# 7 — Storage considerations across AZs

* **EBS**: AZ-local. If a pod using an EBS volume moves to another AZ it cannot attach the same EBS. Strategies:

  * Use **EFS** (regional, multi-AZ) or FSx for shared volumes for cross-AZ file access.
  * For databases, use managed services (RDS Multi-AZ) or use storage replication patterns. ([Amazon Web Services, Inc.][9])
* **Stateful workloads**: consider sticky placement (statefulsets pinned to AZ) + replication across AZ/region.

---

# 8 — Node upgrades / lifecycle (rolling, draining, termination handling)

* Use **Managed Node Groups** for easier rolling upgrades (eksctl or AWS Console). Managed node groups will do rolling replacements and drain nodes. ([AWS Documentation][1])
* Use **Cluster Autoscaler** to scale down empty nodes safely; use `--balance-similar-node-groups`.
* Use **AWS Node Termination Handler** for Spot interruptions/ASG lifecycle events so pods can gracefully drain.
* Use lifecycle hooks & readinessProbes to avoid traffic to draining nodes. ([AWS Documentation][2])

---

# 9 — Multi-region EKS: patterns, routing & data replication

## Two main patterns

1. **Active-Passive (DR)** — primary region serves traffic; secondary region kept warm/standby. Route53 failover or Global Accelerator used for RTO. Useful if cross-region data replication is hard. ([Amazon Web Services, Inc.][9])
2. **Active-Active (global)** — identical stacks deployed in multiple regions; traffic routed by latency/geo via Route53 (latency-based) or **AWS Global Accelerator** for better global health-aware routing. Requires cross-region data strategy. ([Amazon Web Services, Inc.][5])

## Data replication choices

* **Stateless apps**: easiest — just deploy in each region and use edge caches (CloudFront).
* **DynamoDB**: use **Global Tables** for multi-region replication (recommended for key-value workloads). ([Amazon Web Services, Inc.][10])
* **Aurora**: use **Aurora Global Database** for cross-region read replicas and fast recovery for writes (with RTO considerations). ([Amazon Web Services, Inc.][11])
* **RDS / Postgres**: options include logical replication, read replicas, or external replication tools; complexity increases for multi-master needs. ([Amazon Web Services, Inc.][10])

## Traffic routing

* **Route 53**: latency-based or geoproximity routing plus health checks.
* **AWS Global Accelerator**: single anycast IP fronting multiple regional endpoints; better health aware failover and consistent client IPs. Use for low-latency, cross-region failover. ([Amazon Web Services, Inc.][5])

---

# 10 — Multi-cluster management & tooling (what to use)

Use a combination of infra-as-code + GitOps:

### Provisioning / infra

* **eksctl**, **Terraform (aws_eks_cluster / eks module)**, **AWS CDK**, **CloudFormation** for cluster & nodegroup creation. `eksctl` is quick for examples; Terraform/CloudFormation/CDK is recommended for production infra-as-code. ([AWS Documentation][4])

### Dynamic node provisioning

* **Karpenter** — fast, flexible node provisioning (can span instance types/AZs). Good for cost and scale. ([AWS Documentation][8])

### Autoscaling & lifecycle

* **Cluster Autoscaler** — scale down empty nodes safely.
* **Horizontal Pod Autoscaler (HPA)** and **Vertical Pod Autoscaler (VPA)** for app scaling.

### GitOps / multi-cluster app management

* **ArgoCD** or **Flux** — for deploying apps to multiple clusters. Use an app-of-apps pattern for multi-cluster. ([Amazon Web Services, Inc.][12])

### Multi-cluster infra & platform automation

* **Crossplane** — represent cloud resources as Kubernetes CRs, create clusters and cloud infra in a GitOps model. Pair Crossplane (infra) + ArgoCD (app) for full platform automation. ([Amazon Web Services, Inc.][13])

### Observability & operations

* **Prometheus + Grafana** for metrics (federation across regions/clusters).
* **OpenTelemetry / AWS X-Ray** for tracing.
* Centralized logging (CloudWatch / Elasticsearch / OpenSearch).
* **Chaos engineering** tools (Chaos Mesh/Steadybit) to validate failover.

---

# 11 — Concrete multi-region deployment example (high level steps)

1. **Template infra**: use Terraform or CloudFormation to define VPC, subnets (3 AZs), IAM roles, EKS cluster in `us-east-1`. Use same template to provision in `eu-west-1`. (Parameterize region.) ([AWS Documentation][4])
2. **CI/CD & GitOps**: push Helm charts into Git; ArgoCD in a central “platform” cluster deploys apps to regional clusters. ([Amazon Web Services, Inc.][12])
3. **Data**: choose replication strategy (DynamoDB global tables or Aurora Global DB).
4. **Global routing**: use **AWS Global Accelerator** or Route53 latency-based. Test failover by simulating region failure. ([Amazon Web Services, Inc.][5])

---

# 12 — Checklist: production hardening for Multi-AZ / Multi-Region

* [ ] Nodegroups span **≥2 AZs** (ideally 3) per cluster. ([AWS Documentation][1])
* [ ] Multiple node groups for different workload classes (spot, on-demand, GPU). ([Medium][7])
* [ ] Karpenter or Cluster Autoscaler configured. ([AWS Documentation][8])
* [ ] Pod anti-affinity & topologySpreadConstraints to spread pods across AZs. ([AWS Documentation][2])
* [ ] Use EFS/NAS for cross-AZ shared storage or design apps for statelessness. ([Amazon Web Services, Inc.][9])
* [ ] Node Termination Handler + lifecycle hooks for spot/ASG events.
* [ ] Centralized monitoring, logs, and traces with cross-region aggregation.
* [ ] DR plan: runbooks, smoke tests, automated failover validation.

---

# Multi-Region EKS Terraform Module + GitOps (ArgoCD + Crossplane)

This document contains two parts you requested:

1. **Terraform module skeleton** for provisioning multi-region EKS clusters, with AWS Global Accelerator and Aurora Global DB wiring.
2. **GitOps pattern** example using **ArgoCD + Crossplane** (hub-and-spoke) with example manifests and a sample repo layout.

> The content below is meant as a solid, copy-pasteable starting point. Customize names, CIDRs, instance sizes, and policies to match your org standards and control-plane/access model.

---

## Part A — Terraform module skeleton: Multi-region EKS + Global Accelerator + Aurora Global DB

### Goals

* Provision identical EKS clusters in two (or more) regions (e.g., `us-east-1` and `eu-west-1`).
* Provision an Aurora primary cluster in `us-east-1` and an Aurora global secondary in `eu-west-1` (Aurora Global Database).
* Provision AWS Global Accelerator in front of regional ALB endpoints for active-active routing.
* Use Terraform modules and provider aliases to manage multi-region resources in a single repo.

### Directory layout (suggested)

```
infra/
├─ modules/
│  ├─ eks-cluster/            # wrapper module that uses terraform-aws-modules/eks/aws
│  ├─ aurora-global/          # wrapper for terraform-aws-modules/rds-aurora or custom resources
│  └─ global-accelerator/     # wrapper for global accelerator setup
├─ envs/
│  ├─ prod-us-east-1/
│  │  └─ main.tf
│  └─ prod-eu-west-1/
│     └─ main.tf
├─ providers.tf
├─ variables.tf
└─ outputs.tf
```

### providers.tf (multi-region providers + backend)

```hcl
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
}

provider "aws" {
  alias  = "primary"
  region = var.primary_region    # e.g. us-east-1
}

provider "aws" {
  alias  = "secondary"
  region = var.secondary_region  # e.g. eu-west-1
}

# Optional: backend remote state (S3 + DynamoDB locking) config here
```

### root variables.tf

```hcl
variable "primary_region" { type = string }
variable "secondary_region" { type = string }
variable "project" { type = string }
variable "env" { type = string }
```

### Example: instantiating EKS in two regions (envs/prod-us-east-1/main.tf)

```hcl
module "eks_us" {
  source = "../modules/eks-cluster"
  providers = { aws = aws.primary }

  name          = "${var.project}-${var.env}-us"
  region        = var.primary_region
  vpc_id        = var.vpc_id_us
  private_subnets = var.private_subnets_us
  public_subnets  = var.public_subnets_us

  node_groups = var.node_groups_def
}
```

And in `envs/prod-eu-west-1/main.tf`:

```hcl
module "eks_eu" {
  source = "../modules/eks-cluster"
  providers = { aws = aws.secondary }

  name          = "${var.project}-${var.env}-eu"
  region        = var.secondary_region
  vpc_id        = var.vpc_id_eu
  private_subnets = var.private_subnets_eu
  public_subnets  = var.public_subnets_eu

  node_groups = var.node_groups_def
}
```

### Aurora Global DB (module skeleton)

Use the terraform-aws-modules/terraform-aws-rds-aurora module or `aws_rds_global_cluster` + `aws_rds_cluster` resources.

**High-level steps:**

1. Create a primary Aurora cluster in primary region (writer).
2. Create a global cluster (`aws_rds_global_cluster`).
3. Create a secondary Aurora cluster in secondary region and attach it to the global cluster.

**Example simplified (primary region):**

```hcl
resource "aws_rds_global_cluster" "aurora_global" {
  provider = aws.primary
  global_cluster_identifier = "${var.project}-${var.env}-global-db"
}

module "aurora_primary" {
  source  = "terraform-aws-modules/rds-aurora/aws"
  providers = { aws = aws.primary }

  name = "${var.project}-${var.env}-db"
  global_cluster_identifier = aws_rds_global_cluster.aurora_global.id
  engine = "aurora-postgresql"
  engine_version = "14"
  # ... subnet group, vpc security groups, instance_class, parameter group, etc.
}
```

**Secondary region (attach to same global cluster):**

```hcl
module "aurora_secondary" {
  source = "terraform-aws-modules/rds-aurora/aws"
  providers = { aws = aws.secondary }

  name = "${var.project}-${var.env}-db-eu"
  global_cluster_identifier = aws_rds_global_cluster.aurora_global.id
  engine = "aurora-postgresql"
  # ... same config but in eu region, read-only by default
}
```

> Note: Terraform references for `aws_rds_global_cluster` require provider operations in primary region and cross-region resources. The terraform-aws-modules repo has example `global-cluster` patterns (see module docs).

### Global Accelerator wiring

Global Accelerator gives you two static anycast IPs that route to regional endpoints (ALBs, NLBs, or EC2/ECS endpoints).

**High-level approach:**

* Create ALB in each region that fronts your application's Ingress (ALB created by AWS Load Balancer Controller).
* Create an AWS Global Accelerator with endpoint groups for each region, pointing to the regional ALB endpoints (ARNs).
* Use health checks to remove unhealthy regional endpoints.

**Terraform snippet:**

```hcl
module "global_accel" {
  source = "terraform-aws-modules/global-accelerator/aws"
  # or use aws_globalaccelerator_accelerator + endpoint groups directly
  providers = { aws = aws.primary }

  name = "${var.project}-${var.env}-ga"
  enabled = true

  listeners = [
    {
      port_range = { from_port = 443, to_port = 443 }
      protocol = "TCP"
    }
  ]

  endpoint_groups = [
    {
      region = var.primary_region
      endpoint_configuration = [
        {
          endpoint_id = module.alb_us.alb_arn # ALB ARN exported by EKS module or ALB module
        }
      ]
    },
    {
      region = var.secondary_region
      endpoint_configuration = [ { endpoint_id = module.alb_eu.alb_arn } ]
    }
  ]
}
```

**DNS:** create Route53 A (alias) record for `api.example.com` that points to the Global Accelerator static IPs (or use CNAME to the GA DNS name depending on design).

---

## Part B — GitOps pattern: ArgoCD + Crossplane (Hub-and-Spoke)

### Goals

* Use Crossplane to provision cloud infra (EKS clusters, VPCs, RDS/Aurora) as Kubernetes CRs.
* Use ArgoCD to deploy applications & Crossplane compositions in a GitOps fashion.
* Hub-and-spoke model: one "platform" cluster (hub) runs Crossplane + ArgoCD and manages infrastructure for spoke clusters (or you can run ArgoCD centrally and Crossplane in platform cluster).

### Repo layout (example)

```
gitops-root/
├─ platform/                    # Crossplane compositions + provider configs
│  ├─ crossplane/
│  │  ├─ providers/             # providerconfig secrets (AWS creds / IRSA wiring)
│  │  ├─ compositions/          # CompositeResourceDefinitions (XRDs)
│  │  └─ examples/              # example composite resources
│  └─ argocd/                   # ArgoCD App manifests to bootstrap GitOps
├─ clusters/                    # cluster-target-specific overlays
│  ├─ prod-us/                  # Spoke cluster overlay
│  └─ prod-eu/
└─ apps/                        # App Helm charts / kustomize for workloads
   ├─ orders/
   └─ payment/
```

### High-level flow

1. **Platform repo** holds Crossplane compositions (XRDs) that define managed resources like `XCluster` (EKS), `XDatabase` (Aurora global primary), etc. Crossplane `ProviderConfig` uses an IAM user/role or IRSA to manage AWS resources.
2. A developer creates an instance of the composition (XRD) in the `clusters/prod-us` overlay. Crossplane creates the EKS cluster and VPC via Terraform-like providers.
3. ArgoCD watches the `apps/` folders and deploys workloads to the target clusters (using ArgoCD's multi-cluster support). ArgoCD can be bootstrapped by manifests stored in `platform/argocd`.

### Crossplane provider and Composition (example skeleton)

`platform/crossplane/providers/aws-provider.yaml` (ProviderConfig uses IRSA or a secret)

```yaml
apiVersion: pkg.crossplane.io/v1
kind: Provider
metadata:
  name: provider-aws
spec:
  package: "crossplane/provider-aws:latest"
```

`platform/crossplane/compositions/x-eks.yaml` (very simplified)

```yaml
apiVersion: apiextensions.crossplane.io/v1
kind: Composition
metadata:
  name: eks.cluster.aws.example
spec:
  compositeTypeRef:
    apiVersion: infra.example.org/v1alpha1
    kind: XCluster
  resources:
  - name: eks
    base:
      apiVersion: eks.aws.crossplane.io/v1beta1
      kind: Cluster
      spec:
        forProvider:
          region: us-east-1
          version: "1.27"
          roleArn: "{{ .Values.roleArn }}"
        writeConnectionSecretToRef:
          name: eks-conn
    patches: []
```

> Crossplane compositions let you define opinionated, repeatable infra blueprints that a developer can instantiate by creating a `XCluster` resource.

### ArgoCD application sample (to deploy an app to spoke cluster)

`platform/argocd/apps/orders-app.yaml`

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: orders-app
  namespace: argocd
spec:
  project: default
  source:
    repoURL: 'https://github.com/yourorg/gitops-root'
    path: apps/orders/overlays/prod
    targetRevision: HEAD
  destination:
    server: 'https://<spoke-cluster-api-server>'
    namespace: orders
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

### Bootstrapping (recommended steps)

1. Provision a small **platform cluster** (single EKS) manually or via Terraform — this runs Crossplane + ArgoCD.
2. Install Crossplane & provider-aws in platform cluster.
3. Install ArgoCD in platform cluster and configure cluster secrets for each spoke cluster (`argocd cluster add`).
4. Add Crossplane `ProviderConfig` with credentials or IRSA role that allows Crossplane to create resources in target accounts/regions.
5. Create Compositions for common infra pieces (EKS clusters, VPCs, RDS/Aurora).
6. Developers instantiate `XCluster` resources (Crossplane will create the EKS cluster) — ArgoCD will detect the new cluster and deploy workloads to it via `Application` manifests.

### Example: Create a new EKS via Crossplane (XCluster instance)

`clusters/prod-us/xcluster-prod-us.yaml`

```yaml
apiVersion: infra.example.org/v1alpha1
kind: XCluster
metadata:
  name: prod-us
spec:
  compositionSelector:
    matchLabels:
      provider: aws
  parameters:
    region: us-east-1
    nodePools:
      - name: general
        instanceType: m5.large
        minSize: 3
        maxSize: 6
```

Crossplane will synthesize the necessary AWS resources using the `Composition` defined earlier.

---

## Operational notes, best practices & tips

* **State management:** Keep Terraform state or Crossplane remote state securely (S3 + DynamoDB locking). If you use Crossplane, it manages resources via Kubernetes control plane; still maintain Git history for auditing.
* **Secrets & IAM:** Prefer IRSA for Crossplane where possible (Crossplane can assume roles in target accounts). Limit provider permissions to least privilege.
* **Multi-region DB:** Aurora Global DB simplifies reads in multiple regions, but writes go to the primary region. Ensure app read/write patterns support this.
* **Global Accelerator:** use Global Accelerator for stable anycast IPs + health-aware regional routing. Test failover scenarios.
* **Observability:** Centralize logs/metrics; use Prometheus federation or remote write to a global observability backend.
* **Testing:** Script failover tests to validate Global Accelerator + Route53 failover + Aurora failover.

---


---