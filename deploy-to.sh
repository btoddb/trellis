if [ $# != 1 ] ; then
  echo
  echo "usage: $0 <env>"
  echo

  exit
fi

env=$1

capfile=capfile.trellis
baseDir=`dirname $0`
cassVer="1.1.4-notify-SNAPSHOT"
remote_drop_dir="~/drop_spot_trellis"

echo "env = ${env}"

listFile=/btoddb/deploy-envs/env-${env}.list
if ! [ -f ${listFile} ] ; then
  echo
  echo "environment host file, ${listFile}, is missing"
  echo

  exit
fi
for h in `cat ${listFile}` ; do
  if [ -z "${dropHost}" ] ; then
    dropHost="${h}"
    break
  fi
done

echo "dropHost = ${dropHost}"

#
# create tmp local staging
#

echo "Preparing to rsync ..."

rm -fr tmp-staging 2> /dev/null
mkdir tmp-staging
pushd tmp-staging > /dev/null

tar xfz ../trellis-server/apache-cassandra-${cassVer}-bin.tar.gz
tar xfz ../trellis-server/target/trellis-server-0.1.5-SNAPSHOT-server.tar.gz
cp -R ../trellis-server/dir-overlay/* apache-cassandra-${cassVer}/.
ln -s apache-cassandra-${cassVer} cassandra

popd > /dev/null

#
# sync staging to one machine, then sync from that machine to others (avoid slow VPN)
#

rsync --progress --times --recursive --dirs --links --compress --delete --rsh="ssh -o StrictHostKeyChecking=no" tmp-staging/ bburruss@${dropHost}:${remote_drop_dir}/

rm -fr tmp-staging

/btoddb/bin/cap-cmd.sh ${env} ${capfile} rsync_to_others
/btoddb/bin/cap-cmd.sh ${env} ${capfile} deploy_config

