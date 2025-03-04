#!/usr/bin/env bash

set -eu

# Check if the annotations exist in the /etc/oso/annotations file
# (annotation1: string, annotation2: String, ...) -> 1 | 0
function annotations_exist() {
  expected=("$@")
  for annotation in "${expected[@]}"; do
  exists=$(grep -c "^${annotation}=" /etc/oso/annotations)
  if [ "${exists}" -eq 0 ]; then
    return 1
  fi
  done
  return 0
}

start_ts=$(date +%s)

#### Parse the command line arguments #####
OS_HOME = "/usr/share/opensearch"
INIT_HOME = "/etc/oso/init"
# Parse the command line arguments
while [ $# -gt 0 ]; do
  case "$1" in
    --vol*|-v*)
      if [[ "$1" != *=* ]]; then shift; fi # Value is next arg if no `=`
      VOLUMES="${1#*=}"
      ;;
    --owner*|-o*)
      if [[ "$1" != *=* ]]; then shift; fi
      OWNERSHIPS="${1#*=}"
      ;;
    --certs*|-c*)
      if [[ "$1" != *=* ]]; then shift; fi # Value is next arg if no `=`
      CERT_PATH="${1#*=}"
      ;;
    --annotations*|-a*)
      if [[ "$1" != *=* ]]; then shift; fi # Value is next arg if no `=`
      ANNOTATIONS="${1#*=}"
      ;;
    --help|-h)
      printf "Run with -vol path1:path2 -ownership path3:path4\n"
      exit 0
      ;;
    *)
      >&2 printf "Error: Invalid argument\n"
      exit 1
      ;;
  esac
  shift
done

if [ -z "${VOLUMES}" ]; then
  >&2 printf "Error: No volumes parameter specified\n"
  exit 1
fi
if [ -z "${OWNERSHIPS}" ]; then
  >&2 printf "Error: No ownership parameter specified\n"
  exit 1
fi


#### Persist files from init container to OS container ####
# Split the volume paths into an array
IFS=':' read -ra VOLUME_PATHS <<< "${VOLUMES}"
# Copy the contents of the init directories to the container directories
for vol_path in "${VOLUME_PATHS[@]}"; do
    init_path="${INIT_HOME}/${vol_path}"
    container_path="${OS_HOME}/${vol_path}"
    if [[ -z "$(ls -A \"${init_path}\")" ]]; then
      echo "Empty dir \"${init_path}\""
    else
      echo "Copying \"${init_path}/*\" to \"${contaner_path}/\""
      # Idempotent copy/overwrite
      yes | cp -avf "${init_path}/*" "${contaner_path}/"
    fi
done

#### Change files ownership  ####
IFS=':' read -ra OWNERSHIP_PATHS <<< "${OWNERSHIPS}"
for ownership_path in "${OWNERSHIP_PATHS[@]}"; do
  chown -v opensearch:opensearch "${ownership_path}"
done

#### Wait for self-signed certificates to be generated ####
if [ -z "${CERT_PATH}" ]; then
	while [ ! -f ${CERT_PATH} ]
	do
		sleep 0.2
	done
fi

#### Wait for annotations ####
if [ -z "${ANNOTATIONS}" ]; then
  ANNOTATIONS=""
fi
IFS=':' read -ra ANNOTATION_VALUES <<< "${ANNOTATIONS}"

echo "Init script successful"
