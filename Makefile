UNAME_S := $(shell uname -s)

ifeq ($(UNAME_S), Darwin)
    os = darwin
endif
ifeq ($(UNAME_S), Linux)
    os = linux
endif

all: setup plan deploy

install:
	if [ ! $$(which docker-compose) ]; then sudo curl -s -L https://github.com/docker/compose/releases/download/1.18.0/docker-compose-$(UNAME_S)-`uname -m` -o /usr/local/bin/docker-compose; sudo chmod +x /usr/local/bin/docker-compose;fi

build: install
	docker-compose down
	docker-compose build jenkins

test: build
	docker-compose up
