That's a great approach to mastering Kubernetes debugging and advanced practices\!

Here is a full breakdown of the troubleshooting concepts from the provided link, followed by an explanation of advanced topics and tools popular in cloud environments like AWS EKS and Azure AKS.

-----

## 1\. Kubernetes Troubleshooting Scenarios (Q\&A)

Here are 10 real-world Kubernetes troubleshooting scenarios and their solutions, presented in a Question and Answer format:

### 1\. ImagePullBackOff: Private Registry Authentication Failure

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My pods are stuck in **`ImagePullBackOff`** status because they cannot pull images from a private registry. How do I diagnose and fix this? | **A:** **Diagnosis:** Use `kubectl describe pod <pod-name>` and look at the `Events` section for an error like `Error: ErrImagePull` or `repository requires authentication`.<br>**Solution:** Create a **Docker registry secret** (`kubectl create secret docker-registry...`) and update your Deployment/Pod manifest to reference this secret using `imagePullSecrets: - name: regcred`. *Note: On EKS/AKS/GKE, you should typically use **IAM roles** (AWS) or **Service Principals/Managed Identities** (Azure/GCP) attached to the worker nodes instead of manual secrets if you're using the cloud's native container registry.* |

### 2\. CrashLoopBackOff: Application Error on Startup

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My container starts, but immediately crashes and enters a **`CrashLoopBackOff`** state. How do I find the root cause? | **A:** **Diagnosis:** The first step is to check the application logs from the previous failed container instance: `kubectl logs <pod-name> --previous`. This often reveals an application-level error (e.g., "Database connection failed").<br>**Solution:** Check for application dependencies. Use `kubectl get service <dependency-service-name>` to verify its existence. You can also run a temporary debug pod (`kubectl run --rm -it --image=<debug-image> db-test -- bash`) and use tools like `nc` (netcat) or `telnet` to test network connectivity and port reachability to the dependency service. |

### 3\. Environment Variables Missing: ConfigMap Not Mounted

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My application is failing because expected environment variables, which should be sourced from a **ConfigMap**, are missing inside the pod. | **A:** **Diagnosis:** Use `kubectl exec -it <pod-name> -- env` to check the actual environment variables in the running container. If they are missing, check the ConfigMap itself: `kubectl get configmap <name>` and `kubectl describe configmap <name>`.<br>**Solution:** Verify that your Deployment's Pod specification correctly uses the `envFrom` field, specifically `configMapRef: name: <configmap-name>`, to inject all key/value pairs from the ConfigMap into the container's environment. |

### 4\. Service Connectivity: Microservice Cannot Reach Database

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My application pod cannot connect to a backend service (e.g., a database) despite both running in the cluster. What's the network debugging process? | **A:** **Diagnosis/Solution:**

1.  **Test DNS Resolution:** Run a debug pod and use `nslookup <service-name>` to ensure the service name resolves to an IP address.
2.  **Check Endpoints:** Use `kubectl get endpoints <service-name>`. The service must have **endpoints** (the IP addresses of the ready pods) listed. If the list is empty, the **Service selector** likely does not match the **Pod labels**.
3.  **Check Network Policies:** Use `kubectl get networkpolicies` to see if any policy is blocking traffic from the application pod to the database service. |

### 5\. Ingress Not Working: Load Balancer Creation Failed

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** I created an **Ingress** resource, but it never gets an external IP, and the cloud load balancer is not provisioned. | **A:** **Diagnosis:** Use `kubectl describe ingress <ingress-name>` and check the **Events** section. A common cloud-specific error is `Failed to create load balancer, no subnet found with required tags`.<br>**Solution:**

1.  **Check Ingress Controller:** Verify that an Ingress Controller (like NGINX, or a cloud-provider specific one) is installed and running (`kubectl get pods -n ingress-nginx`).
2.  **Cloud-Specific Tags:** For cloud environments (like AWS EKS), ensure the underlying subnets have the **required tags** that the Kubernetes cloud controller manager needs to provision a Load Balancer (e.g., `kubernetes.io/role/elb`). |

### 6\. Pod Stuck in Pending: Resource Constraints

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My pod is stuck in the **`Pending`** state and is not being scheduled to any node. | **A:** **Diagnosis:** Use `kubectl describe pod <pod-name>` and look at the **Events**. A frequent message is `0/N nodes are available: N Insufficient memory` (or CPU). This means no node has enough available resources to satisfy the Pod's **resource requests**.
<br>**Solution:**

1.  **Check Node Resources:** Use `kubectl describe nodes` and look at the `Allocated resources` section to see how much capacity is left.
2.  **Adjust Pod Requests:** Reduce the `resources.requests` values in your pod spec to see if it can fit.
3.  **Scale Cluster:** If the workload genuinely needs more resources, **scale up** the number of nodes in your cluster (e.g., by adjusting the AWS Auto Scaling Group or AKS node pool). |

### 7\. Pod Evicted: Node Out of Resources

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My pods are occasionally getting **`Evicted`**. Why does this happen? | **A:** **Diagnosis:** `kubectl describe pod <pod-name>` will show a message like `The node was low on resource: memory`. This occurs when a node is under **resource pressure** (MemoryPressure, DiskPressure, etc.), and the Kubelet decides to terminate a low-priority pod to free up resources.<br>**Solution:**

1.  **Find the Cause:** Use `kubectl top pods --all-namespaces --sort-by=memory` to find the pods consuming the most resources.
2.  **Set Limits:** Ensure all your deployments have **resource limits** set (`resources.limits`), as this prevents containers from consuming infinite resources and causing node pressure.
3.  **Autoscaling:** Use a **Node Autoscaler** (like Karpenter or Cluster Autoscaler) to automatically provision new nodes when the cluster is running low on resources. |

### 8\. Service Account Permissions: Pod Can't Access API Server

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My pod, which is running a Kubernetes controller/operator, is receiving **`Forbidden`** errors when trying to interact with the Kubernetes API Server. | **A:** **Diagnosis:** Check the pod's logs (`kubectl logs <pod-name>`) for authorization errors like `Forbidden: cannot list deployments.apps in the namespace default`. This is a **Role-Based Access Control (RBAC)** issue.<br>**Solution:**

1.  **Create ServiceAccount:** If not already done, create a dedicated `ServiceAccount`.
2.  **Create Role/ClusterRole:** Define a `Role` (namespace-specific) or `ClusterRole` (cluster-wide) with the exact permissions (verbs and resources) the pod needs.
3.  **Create RoleBinding/ClusterRoleBinding:** Bind the `Role/ClusterRole` to the `ServiceAccount`.
4.  **Assign to Pod:** Ensure the Deployment's Pod spec uses `serviceAccountName: <name>`. |

### 9\. Persistent Volume Claim Stuck in Pending: Storage Class Issues

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My **Persistent Volume Claim (PVC)** is stuck in the **`Pending`** state, and the stateful pod cannot start. | **A:** **Diagnosis:** Use `kubectl describe pvc <pvc-name>` and check the `Events`. The message often says `storageclass.storage.k8s.io "<storageclass-name>" not found`.<br>**Solution:**

1.  **Check StorageClass:** Verify the available storage classes with `kubectl get storageclass`.
2.  **Update PVC:** Ensure the PVC's `storageClassName` field is set to an **existing** and **correctly configured** StorageClass.
3.  *Cloud-Specific:* In EKS/AKS, ensure that the necessary **CSI drivers** (Container Storage Interface) are installed and the IAM roles attached to the worker nodes have permissions to provision storage (e.g., EBS volumes on AWS). |

### 10\. Container Terminated with OOMKilled: Memory Limits Too Low

| Question | Answer/Solution |
| :--- | :--- |
| **Q:** My container keeps getting terminated with the status **`OOMKilled`** (Out-Of-Memory Killed). | **A:** **Diagnosis:** Use `kubectl describe pod <pod-name>` and look in the `State: Terminated` section for `Reason: OOMKilled`. This means the container exceeded its configured **memory limit**.
<br>**Solution:**

1.  **Check/Increase Limits:** Review the pod's resource configuration and **increase the memory `limits`** (and potentially `requests`) in the container spec.
2.  **Application Profiling:** If increasing the limit is not sustainable, **profile the application** for memory leaks.
3.  **Monitoring:** Use monitoring tools like **`kubectl top pods`** (requires Metrics Server) to track actual memory usage over time and set an appropriate limit. |

-----

## 2\. Advanced Kubernetes Topics and Tools

Beyond basic troubleshooting, the Kubernetes ecosystem is rapidly adopting several advanced tools and practices to enhance security, observability, and management, especially in managed services like **AWS EKS** and **Azure AKS**.

### A. Observability Stack (Metrics, Logs, Traces)

The key to advanced debugging is having a complete picture of your distributed system. Observability, the ability to infer the internal state of a system from external outputs, is crucial.
| Pillar | Key Tool/Technology | EKS/AKS Context |
| :--- | :--- | :--- |
| **Metrics** (What's happening now?) | **Prometheus** & **Grafana** | **EKS/AKS:** Often deployed via Helm charts. AWS offers **Amazon Managed Service for Prometheus (AMP)**, and Azure offers a similar managed service. Grafana dashboards provide visualization. |
| **Logs** (What happened?) | **Loki**, **Fluentd/Fluent Bit** (Log Shipper) | **EKS/AKS:** Logs are typically collected from the cluster and forwarded to a centralized store like AWS CloudWatch Logs, Azure Monitor, or a self-hosted **Loki/Elasticsearch** stack. |
| **Traces** (Where did the time go?) | **Jaeger** or **Zipkin** (via **OpenTelemetry**) | **EKS/AKS:** Modern microservices use **OpenTelemetry** to instrument code. Traces are collected and visualized to show the full path of a request across services, helping to pinpoint latency bottlenecks. |

### B. GitOps for CI/CD

**GitOps** is a modern approach to continuous delivery that uses Git as the single source of truth for the system's declarative infrastructure and applications.

| Tool/Concept | Description | Benefit |
| :--- | :--- | :--- |
| **Argo CD** and **Flux** | Leading GitOps tools that continuously monitor a Git repository and automatically reconcile the cluster state (EKS/AKS) to match the configuration defined in Git. | Provides a clear **audit trail** for every change, simplifies disaster recovery (just re-apply the Git repo), and automatically detects **configuration drift**. |
| **Argo Rollouts** | An advanced deployment controller that integrates with Argo CD to enable sophisticated progressive delivery strategies (e.g., **Canary Deployments** and **Blue/Green Deployments**). | Increases deployment safety by gradually shifting traffic and performing automated analysis before a full rollout. |

### C. Advanced Networking and Security

| Tool/Concept | Description | EKS/AKS Context |
| :--- | :--- | :--- |
| **Service Mesh (Istio / Linkerd)** | A dedicated infrastructure layer for managing service-to-service communication. It provides advanced features like traffic routing, request-level encryption (mTLS), and fine-grained observability.  | Often deployed on top of EKS/AKS to handle complex microservice traffic. AWS App Mesh is a managed service that can be used with EKS. |
| **eBPF (Extended Berkeley Packet Filter)** | A revolutionary kernel technology that allows for powerful tracing, networking, and security functions to be loaded dynamically into the Linux kernel without changing the kernel source code. | Tools like **Cilium** (which uses eBPF) are replacing traditional **kube-proxy** and can provide faster, more secure, and highly observable networking and Network Policy enforcement in EKS/AKS. |
| **Policy as Code (OPA Gatekeeper / Kyverno)** | Tools that allow you to define and enforce policies (rules) for your cluster resources (e.g., "all pods must have resource limits," or "no service can run as root"). **Gatekeeper** is the Kubernetes implementation of **Open Policy Agent (OPA)**. | Enforces security and compliance best practices automatically upon resource creation in the EKS/AKS API server, preventing bad configurations from ever being deployed. |

### D. Cloud-Specific Tools for EKS and AKS

| Cloud Provider | Advanced Tool/Feature | Use Case |
| :--- | :--- | :--- |
| **AWS EKS** | **Karpenter** (Node Autoscaler) | A highly efficient, open-source node autoscaler that works with AWS EC2 Spot Instances to provision the *right* compute resources (instance type, size) for your pending pods in seconds, often replacing the older Cluster Autoscaler. |
| **AWS EKS** | **Amazon EKS Blueprints for Terraform/CloudFormation** | Templates and patterns that allow you to quickly provision secure, multi-account, highly available EKS clusters with all the necessary add-ons (monitoring, logging, GitOps) already configured. |
| **Azure AKS** | **Azure Monitor Container Insights** | A deep-dive monitoring service that collects performance metrics from nodes, controllers, and containers within AKS. It provides native health and resource views directly in the Azure Portal. |
| **Azure AKS** | **Azure Policy for Kubernetes** | Extends Azure Policy to manage and report on the compliance status of your AKS clusters. It uses OPA Gatekeeper to enforce custom policies at the cluster level. |
| **Cost Management** | **Kubecost** | A tool used across all clouds (EKS/AKS/GKE) that provides real-time visibility into Kubernetes spending, including allocation, optimization recommendations, and chargebacks. **Kubecost is one of the most trending tools for FinOps in Kubernetes.** |