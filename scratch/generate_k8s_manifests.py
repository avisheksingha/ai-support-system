import os

services = {
    "ai-analysis-service": {
        "port": 8083,
        "secrets": ["DB_PASSWORD", "DB_USERNAME", "OPENAI_API_KEY", "GOOGLE_API_KEY"]
    },
    "routing-service": {
        "port": 8084,
        "secrets": ["DB_PASSWORD", "DB_USERNAME", "JWT_SECRET"]
    },
    "rag-service": {
        "port": 8085,
        "secrets": ["DB_PASSWORD", "DB_USERNAME", "OPENAI_API_KEY", "GOOGLE_API_KEY"]
    }
}

base_path = "f:/Workspace/ai-support-system/infra/k8s/base/applications"

for svc, config in services.items():
    app_dir = os.path.join(base_path, svc)
    os.makedirs(app_dir, exist_ok=True)
    
    # 1. deployment.yaml
    deployment_content = f"""apiVersion: apps/v1
kind: Deployment
metadata:
  name: {svc}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: {svc}
  template:
    metadata:
      labels:
        app: {svc}
    spec:
      containers:
        - name: {svc}
          image: {svc}:v1
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: {config['port']}
              name: http
          envFrom:
            - configMapRef:
              name: {svc}-config
          env:
"""
    for secret in config['secrets']:
        deployment_content += f"""            - name: {secret}
              valueFrom:
                secretKeyRef:
                  name: ai-support-secrets
                  key: {secret}
"""
    deployment_content += f"""          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            initialDelaySeconds: 60
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 6
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            initialDelaySeconds: 120
            periodSeconds: 15
            timeoutSeconds: 5
            failureThreshold: 3
          resources:
            requests:
              cpu: "50m"
              memory: "256Mi"
            limits:
              cpu: "500m"
              memory: "512Mi"
"""
    with open(os.path.join(app_dir, "deployment.yaml"), "w") as f:
        f.write(deployment_content)

    # 2. service.yaml
    service_content = f"""apiVersion: v1
kind: Service
metadata:
  name: {svc}
spec:
  selector:
    app: {svc}
  ports:
    - protocol: TCP
      port: {config['port']}
      targetPort: {config['port']}
"""
    with open(os.path.join(app_dir, "service.yaml"), "w") as f:
        f.write(service_content)

    # 3. configmap.yaml
    configmap_content = f"""apiVersion: v1
kind: ConfigMap
metadata:
  name: {svc}-config
data:
  SPRING_PROFILES_ACTIVE: "docker,k8s"
"""
    with open(os.path.join(app_dir, "configmap.yaml"), "w") as f:
        f.write(configmap_content)

    # 4. kustomization.yaml
    kustomization_content = f"""apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - deployment.yaml
  - service.yaml

configMapGenerator:
  - name: {svc}-config
    behavior: replace
    files:
      - configmap.yaml
"""
    with open(os.path.join(app_dir, "kustomization.yaml"), "w") as f:
        f.write(kustomization_content)

print("Generated manifests for all 3 services.")
