# global service name
SERVICE                 := umls-terminology-service

INTERACTIVE := $(shell [ -t 0 ] && echo 1)
#######################################################################
#                 OVERRIDE THIS TO MATCH YOUR PROJECT                 #
#######################################################################

# Most applications have their own method of maintaining a version number.
APP_VERSION             := $(shell echo `grep "<version>" pom.xml | perl -pe 's/ *<\/?version>//g'`)
GIT_VERSION             ?= $(shell echo `git describe --match=NeVeRmAtCh --always --dirty`)
GIT_COMMIT              ?= $(shell echo `git log | grep -m1 -oE '[^ ]+$'`)
GIT_COMMITTED_AT        ?= $(shell echo `git log -1 --format=%ct`)
GIT_BRANCH              ?=

.PHONY: target

# Clean build artifacts. Override for your project
clean:
	./mvnw clean

test:
	./mvnw test

install:
	./mvnw -Dconfig.artifactId=term-server-config-prod install

adminlucene:
	mvn install -PReindex -Drun.config.umls=$(config)

# Publish artifacts to nexus (requires a local .gradle/gradle.properties propery configured)
#release:
#	./mvnw TBD

version:
	@echo $(APP_VERSION)
