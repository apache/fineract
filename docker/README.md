Installing Fineract with Docker and docker-compose

Prerequisites
=============
* docker and docker-compose installed on your machine


Installing a new Fineract instance
==================================

* Clone the Fineract Github repository
* Navigate to the docker directory
* Run the following commands:
    * docker-compose build
    * docker-compose up -d
* Fineract will run at https://localhost:8443/fineract-provider


Using docker-compose for development
====================================

* Copy Dockerfile, docker-compose.yml, and the initdb directory to the root of your project (/fineract)
* Edit Dockerfile in /fineract - comment out the 2 lines (RUN git clone and COPY build.gradle)
* Uncomment the 2 lines in Dockerfile (RUN mkdir fineract, COPY . fineract)
* Update your local copy of build.gradle and replace all references to localhost with fineractmysql
