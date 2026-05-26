#!/bin/bash

set -e

scriptName='01_helm_lint.sh'

HELP=unset
TEST=unset
NOCLEANUP=unset

usage()
{
  echo "
$scriptName

Usage:
  $scriptName --help
  $scriptName [ --noclean ]
  $scriptName --test [ --test myTest] [ --noclean ]

Flags:
  [ -h | --help ]
  [ -C | --nocleanup ]
  [ -t | --test ]       # run specific test
"
  exit 2
}

PARSED_ARGUMENTS=$(getopt -a -n $scriptName -o hCt: --long help,nocleanup,test: -- "$@")
VALID_ARGUMENTS=$?
if [ "$VALID_ARGUMENTS" != "0" ]; then
  usage
fi

eval set -- "$PARSED_ARGUMENTS"
while :
do
  case "$1" in
    -h | --help)    HELP=1       ; shift   ;;
    -C | --nocleanup) NOCLEANUP=1    ; shift   ;;
    -t | --test)   TEST=1      ; shift 2 ;;
    # -- means the end of the arguments; drop this, and break out of the while loop
    --) shift; break ;;
    # If invalid options were passed, then getopt should have reported an error,
    # which we checked as VALID_ARGUMENTS when getopt was called...
    *) echo "Unexpected option: $1 - this should not happen."
       usage ;;
  esac
done

if [ "$HELP" != "unset" ]; then
  usage
fi

docleanup=1
if [ "$NOCLEANUP" != "unset" ]; then
  docleanup=0
fi

test=
if [ "$TEST" != "unset" ]; then
  if [ "$TEST" == "" ]; then
    echo "ERROR: empty test given"
    exit 1;
  fi
  test=$IMAGE
fi

helmLintLogFile=test/01_helm_lint.log
if [ -z "$test" ]; then
  valueFilesToTest=$(find test -name "values.*.yaml")
else
  valueFilesToTest=$(find test -wholename "$test")
fi
count=$(echo -n "$valueFilesToTest" | wc -w )
index=1
rm -f helmLintLogFile
for vftt in $valueFilesToTest; do
  #generate a value file without generic-application-pipeline root key
  yq '.generic-application-pipeline as $root | {} | . = $root' "${vftt}" > "$vftt.tmp"

  helmCmd="helm lint --strict -f values.yaml -f $vftt.tmp ."
  echo "[TEST] Helm lint: Step $index/$count: $helmCmd ..."
  $helmCmd >> $helmLintLogFile
  echo "[TEST] Helm lint: Step $index/$count: $helmCmd OK"
  index=$((index+1))
  if [ "$docleanup" -eq 1 ]; then
    rm -f "$vftt.tmp"
  fi
done

# other tests
#echo "[TEST] Other templatings: test/values.test.yaml does not contains any azure reference ..."
#helm template -f values.yaml -f test/values.test.yaml . 2> >(grep -v 'found symbolic link' >&2) | grep -vE '^\s*#' | grep -iE '(azure|cedrico)' | wc -l | grep 0
#echo "[TEST] Other templatings: test/values.test.yaml does not contains any azure reference OK"

anyError=$(cat $helmLintLogFile | grep 'chart(s) linted' | grep -vc '0 chart(s) failed' || true)
if [ "$anyError" -ne 0 ]; then
  echo "[TEST] FAILURE, at least one error"
  cat $helmLintLogFile
  false
else
  echo "[TEST] SUCCESS"
  if [ "$docleanup" -eq 1 ]; then
      rm -f $helmLintLogFile
    fi
  true
fi



