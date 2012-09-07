kill $(ps aux | grep 'TrellisCassandraDaemon' | grep -v 'grep' | awk '{print $2}')
