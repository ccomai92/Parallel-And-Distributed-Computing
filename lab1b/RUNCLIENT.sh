#! /bin/bash

echo server_ip?:

read var3

java TcpClientDouble 4798 10 $var3
