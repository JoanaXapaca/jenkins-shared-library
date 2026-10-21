\# Jenkins Shared Library - ConnexALL Pipeline



Biblioteca partilhada para pipelines CI/CD dos projetos ConnexALL.



\## Uso



No Jenkinsfile de cada projeto:



```groovy

@Library('connexall-pipeline') \_



connexallPipeline()


Estrutura
vars/connexallPipeline.groovy - Pipeline principal

src/com/connexall/pipeline/ - Classes auxiliaries


