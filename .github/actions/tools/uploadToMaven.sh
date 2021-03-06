#!/bin/bash
#############################################
#
# Usage
#       uploadAllToMaven path/of/dist/maven https://api.bintray.com/maven/riccardo/sandbox-maven/ riccardo $BINTRAY_PASSWORD gitrepo license
#           Note: gitrepo and license are needed only when uploading to bintray if you want to create missing packages automatically
#                   gitrepo must be a valid source repository
#                   license must be a license supported by bintray eg "BSD 3-Clause"
#   or
#       uploadAllToMaven path/of/dist/maven $GITHUB_PACKAGE_REPOSITORY user password
#
#############################################
root="`dirname  ${BASH_SOURCE[0]}`"

set -e
function uploadToMaven {
    file="$1"
    destfile="$2"
    repourl="$3"
    user="$4"
    password="$5"
    srcrepo="$6"
    license="$7"

    auth=""

    if [ "$user" != "token" ];
    then
        echo "Upload with username $user and password"
        auth="-u$user:$password"
    else
        echo "Upload with token"
        auth="-H \"Authorization: token $password\""
    fi

    

    cmd="curl -T \"$file\" $auth \
        \"$repourl/$destfile\" \
        -vvv"

    echo "Run $cmd"
    eval "$cmd"    
}
export -f uploadToMaven

function uploadAllToMaven {
    path="$1"
    cdir="$PWD"
    cd "$path"
    files="`find . \( -name "*.jar" -o -name "*.pom" \) -type f -print`"
    IFS="
"
    set -f
    for art in $files; do
        art="${art:2}"
        uploadToMaven "$art" "$art" ${@:2}   
    done
    set +f
    unset IFS

    cd "$cdir"
} 
