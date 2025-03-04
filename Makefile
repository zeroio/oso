help:		## Show this help.
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

install-crd:	## Install the CRDs into the K8s cluster
	@echo Install CRDS into the K8s cluster
	@kubectl apply -f operator/build/resources/main/clusters.oso.io-v1.yml
