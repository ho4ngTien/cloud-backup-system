#!/bin/bash
# =============================================================
#  CloudSafe – Google Cloud Platform Setup Script
#  Giai đoạn 2: Tạo toàn bộ hạ tầng GCP
#
#  Prerequisites:
#    - gcloud CLI installed & logged in
#    - Billing account enabled
#
#  Usage:
#    chmod +x setup.sh
#    ./setup.sh
# =============================================================

set -e  # exit on error

# ===== CONFIG – Thay đổi theo dự án của bạn =====
PROJECT_ID="cloudsafe-$(date +%Y%m%d)"   # e.g. cloudsafe-20240102
REGION="asia-southeast1"                  # Singapore
ZONE="${REGION}-a"
INSTANCE_NAME="cloudsafe-vm"
MACHINE_TYPE="e2-micro"                   # Free-tier eligible
BUCKET_NAME="cloudsafe-backup-${PROJECT_ID}"
SA_NAME="cloudsafe-sa"
SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
FIREWALL_TAG="cloudsafe-server"

echo "============================================"
echo "  CloudSafe GCP Setup"
echo "  Project : $PROJECT_ID"
echo "  Region  : $REGION"
echo "============================================"
echo

# ---------------------------------------------------
# Step 1: Create Project
# ---------------------------------------------------
echo "▶ Step 1: Creating GCP project..."
gcloud projects create "${PROJECT_ID}" --name="CloudSafe Backup System" || true
gcloud config set project "${PROJECT_ID}"
echo "   ✅ Project: ${PROJECT_ID}"

# Wait for project to propagate
sleep 5

# ---------------------------------------------------
# Step 2: Link Billing (must be done in Console)
# ---------------------------------------------------
echo
echo "⚠️  MANUAL STEP: Link a billing account to project '${PROJECT_ID}' in GCP Console."
echo "   https://console.cloud.google.com/billing/linkedaccount?project=${PROJECT_ID}"
echo "   Press ENTER when done..."
read -r

# ---------------------------------------------------
# Step 3: Enable APIs
# ---------------------------------------------------
echo "▶ Step 3: Enabling required APIs..."
gcloud services enable \
  compute.googleapis.com \
  storage.googleapis.com \
  iam.googleapis.com \
  cloudresourcemanager.googleapis.com \
  sqladmin.googleapis.com \
  --project="${PROJECT_ID}"
echo "   ✅ APIs enabled"

# ---------------------------------------------------
# Step 4: Create Service Account
# ---------------------------------------------------
echo
echo "▶ Step 4: Creating Service Account..."
gcloud iam service-accounts create "${SA_NAME}" \
  --display-name="CloudSafe Service Account" \
  --project="${PROJECT_ID}" || true

# Grant Storage Object Admin role
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/storage.objectAdmin"

# Grant Compute Instance Admin (for CI/CD SSH)
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/compute.instanceAdmin.v1"

# Export credentials JSON
gcloud iam service-accounts keys create "./cloudsafe-sa-key.json" \
  --iam-account="${SA_EMAIL}" \
  --project="${PROJECT_ID}"
echo "   ✅ Service account created, key saved to ./cloudsafe-sa-key.json"
echo "   ⚠️  Add this key as GCP_SA_KEY in GitHub Secrets!"

# ---------------------------------------------------
# Step 5: Create Cloud Storage Bucket
# ---------------------------------------------------
echo
echo "▶ Step 5: Creating Cloud Storage bucket..."
gsutil mb -p "${PROJECT_ID}" -c STANDARD -l "${REGION}" "gs://${BUCKET_NAME}" || true
gsutil versioning set on "gs://${BUCKET_NAME}"
gsutil lifecycle set - "gs://${BUCKET_NAME}" << 'EOF'
{
  "rule": [
    {
      "action": {"type": "Delete"},
      "condition": {"age": 365, "isLive": false}
    }
  ]
}
EOF
echo "   ✅ Bucket: gs://${BUCKET_NAME} (versioning enabled, 1-year retention)"

# ---------------------------------------------------
# Step 6: Create Firewall Rules
# ---------------------------------------------------
echo
echo "▶ Step 6: Creating firewall rules..."
gcloud compute firewall-rules create "allow-http-8080" \
  --project="${PROJECT_ID}" \
  --allow="tcp:8080" \
  --target-tags="${FIREWALL_TAG}" \
  --source-ranges="0.0.0.0/0" \
  --description="Allow CloudSafe API port" || true

gcloud compute firewall-rules create "allow-https-443" \
  --project="${PROJECT_ID}" \
  --allow="tcp:443,tcp:80" \
  --target-tags="${FIREWALL_TAG}" \
  --source-ranges="0.0.0.0/0" \
  --description="Allow HTTP/HTTPS for Nginx" || true
echo "   ✅ Firewall rules created"

# ---------------------------------------------------
# Step 7: Create Compute Engine VM
# ---------------------------------------------------
echo
echo "▶ Step 7: Creating Compute Engine VM..."
gcloud compute instances create "${INSTANCE_NAME}" \
  --project="${PROJECT_ID}" \
  --zone="${ZONE}" \
  --machine-type="${MACHINE_TYPE}" \
  --image-family="debian-12" \
  --image-project="debian-cloud" \
  --boot-disk-size="20GB" \
  --boot-disk-type="pd-standard" \
  --service-account="${SA_EMAIL}" \
  --scopes="https://www.googleapis.com/auth/cloud-platform" \
  --tags="${FIREWALL_TAG}" \
  --metadata="startup-script=#! /bin/bash
    apt-get update -y
    apt-get install -y docker.io docker-compose-plugin curl
    systemctl enable docker
    systemctl start docker
    gcloud auth configure-docker --quiet
    echo 'VM ready!'
  "
echo "   ✅ VM created: ${INSTANCE_NAME} (${MACHINE_TYPE}) in ${ZONE}"

# ---------------------------------------------------
# Step 8: Get VM External IP
# ---------------------------------------------------
echo
EXTERNAL_IP=$(gcloud compute instances describe "${INSTANCE_NAME}" \
  --zone="${ZONE}" --project="${PROJECT_ID}" \
  --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
echo "   🌐 External IP: ${EXTERNAL_IP}"

# ---------------------------------------------------
# Summary
# ---------------------------------------------------
echo
echo "============================================"
echo "  ✅ GCP Setup Complete!"
echo "============================================"
echo
echo "  Project ID   : ${PROJECT_ID}"
echo "  VM IP        : ${EXTERNAL_IP}"
echo "  Bucket       : gs://${BUCKET_NAME}"
echo "  SA Key       : ./cloudsafe-sa-key.json"
echo
echo "  Next steps:"
echo "  1. Add these GitHub Secrets:"
echo "     GCP_PROJECT_ID  = ${PROJECT_ID}"
echo "     GCP_SA_KEY      = (contents of cloudsafe-sa-key.json)"
echo "     GCE_INSTANCE_NAME = ${INSTANCE_NAME}"
echo "     GCE_ZONE        = ${ZONE}"
echo "     GCP_BUCKET_NAME = ${BUCKET_NAME}"
echo "     JWT_SECRET      = (generate a 256-bit random string)"
echo "     MAIL_USERNAME   = (your Gmail)"
echo "     MAIL_PASSWORD   = (Gmail App Password)"
echo
echo "  2. SSH into VM: gcloud compute ssh ${INSTANCE_NAME} --zone=${ZONE}"
echo "  3. Push to main branch to trigger CI/CD deployment"
echo
