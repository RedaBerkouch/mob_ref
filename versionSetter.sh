#Note: Use this script with the following command: ./versionSetter.sh <version> <buildNumber>
#Check if version is passed as argument $1 major $2 minor
RED='\033[0;31m'
NC='\033[0m' # No Color
isVersionParameterDefined() {
  if [ -n "$1" ] && [ -n "$2" ]; then
    true
  else
    false
  fi
}

updateFile() {
  cd $i || fail "Failed to change directory to $i"
  echo "Updating version.properties file with version $1.$2 for $i"
  echo "version.major=$1" >|resources/version.properties
  echo "version.minor=$2" >>resources/version.properties
  cd .. || fail "Failed to change directory to .."
}

if isVersionParameterDefined "$1" "$2" == "true"; then
  mebArray=("mebweb" "sbaweb" "sdlweb" "sspweb" "sbgweb")
  for i in "${mebArray[@]}"; do
    {

      updateFile "$1" "$2" "$i"
    } || {
      printf " ${RED}Error while generating file version for $i ${NC}\n"
    }
  done
else
  echo "Please provide a version number and a build number"
fi
