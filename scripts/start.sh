#!/bin/bash

deployServer() {
   echo "yl-toggle Server starting deploy."
   /usr/local/bin/yl-toggle-server
   check_num=0
	while true; do
		if /usr/local/bin/healthcheck "http://127.0.0.1:${YT_SERVER_PORT}/internal/all_secrets"; then
		    echo "yl-toggle Server deplay successfully."
		    exit 0
		elif [[ $check_num == 150 ]];then
			echo "yl-toggle Server deployment fail."
			exit 1
		fi
		sleep 2
		((check_num++))
		echo "yl-toggle Server deployment in progress..."
	done
}

deployAnalysis() {
    echo "yl-toggle Analysis starting deploy."
    java ${ANALYSIS_JVM_ARGS} -jar /usr/local/bin/analysis.jar --server.port=${analysis_server_port} --spring.profiles.active=${analysis_spring_profiles_active} &
    check_num=0
	while true; do
		if /usr/local/bin/healthcheck "http://127.0.0.1:${analysis_server_port}/actuator/health"; then
		    echo "yl-toggle Analysis deplay successfully."
		    if [[ $1 == 0 ]];then
		    	deployServer
			fi		   
		    exit 0
		elif [[ $check_num == 150 ]];then
			echo "yl-toggle Analysis deployment fail."
			exit 1
		fi
		((check_num++))
		sleep 2
		echo "yl-toggle Analysis deployment in progress..."
	done
}

deployApi() {
    java ${API_JVM_ARGS} -jar /usr/local/bin/api.jar --server.port=${api_server_port} --spring.profiles.active=${api_spring_profiles_active} & 
    check_num=0
    while true; do
        if /usr/local/bin/healthcheck "http://127.0.0.1:${api_server_port}/actuator/health"; then
            echo "yl-toggle API deplay successfully."
            if [[ $1 == 0 ]];then
                deployAnalysis 0
                
            fi		   
            exit 0
        elif [[ $check_num == 150 ]];then
            echo "yl-toggle API deployment fail."
            exit 1
        fi
        ((check_num++))
        sleep 2
        echo "yl-toggle API deployment in progress..."
    done
}

deployApi 0
exit 1;
