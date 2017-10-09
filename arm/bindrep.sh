#!/bin/bash
docker run -v $2:/var/lib/postgresql/data -p 5432:5432 -t $1
