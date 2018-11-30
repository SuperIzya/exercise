#!/bin/bash

cub zk-ready "${ZK_HOST}:${ZK_PORT}" 120 2 60

/data/pack/bin/watchZK
