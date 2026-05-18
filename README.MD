# MEP

copy of source code from meb-sourcecode_2022-05-06.zip

## Build

Needed
 * Maven 3.X
 * JAVA JDK 8
      
### Url du front : http://localhost:7001/sbaweb/frontFolder/home

### External Maven dependencies

fetch from [Document>General> 04 - Installer > MEB > Librairies](https://swordoffice.sharepoint.com/:f:/r/sites/SES-SES-TMAOFCL/Documents%20partages/General/04%20-%20Installers/MEB/Librairies?csf=1&web=1&e=K1dtf2) 
```
mvn install:install-file -Dfile=com.bea.core.utils.full-1.4.0.0.jar -DgroupId=weblogic -DartifactId=com.bea.core.utils.full -Dversion=1.4.0.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=javax.xml.rpc-1.2.1.jar             -DgroupId=weblogic -DartifactId=javax.xml.rpc -Dversion=1.2.1 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=wseeclient-10.3.jar                 -DgroupId=weblogic -DartifactId=wseeclient -Dversion=10.3 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=glassfish.stax.ex-1.0.0.0_1-2.jar   -DgroupId=weblogic -DartifactId=glassfish.stax.ex -Dversion=1.0.0.0_1-2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=javax.jws-2.0.jar                   -DgroupId=weblogic -DartifactId=javax.jws -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=mwx-common-login-1.0.0.jar -DgroupId=ch.admin.astra.bit -DartifactId=mwx-common-login -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=sasConnection-9.2.jar -DgroupId=com.sas -DartifactId=sasConnection -Dversion=9.2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=sasSecurity-9.2.jar   -DgroupId=com.sas -DartifactId=sasSecurity -Dversion=9.2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=sasCore-9.2.jar       -DgroupId=com.sas -DartifactId=sasCore -Dversion=9.2 -Dpackaging=jar -DgeneratePom=true
```

### Intellij 
disable all profile except local in maven configuration


## Local Run

### Install Weblogic

### Configure Weblogic

### Configure DB

### Azure configuration

#### Configure credentials
change in ```~/.m2/settings.xml```

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>ofsdependencies</id>
      <username>SWG-SES-OFSPROJECT</username>
      <password>AZURE_PAT_PACKAGE_ACCESS</password>
    </server>
  </servers>
</settings>
```
#### Upload maven artefact

this step should be done again (only needed once)

```mvn deploy:deploy-file -Dfile=com.bea.core.utils.full-1.4.0.0.jar -DgroupId=weblogic -DartifactId=com.bea.core.utils.full -Dversion=1.4.0.0 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```

```mvn deploy:deploy-file -Dfile=wseeclient-10.3.jar -DgroupId=weblogic -DartifactId=wseeclient -Dversion=10.3 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```

```mvn deploy:deploy-file -Dfile=glassfish.stax.ex-1.0.0.0_1-2.jar -DgroupId=weblogic -DartifactId=glassfish.stax.ex -Dversion=1.0.0.0_1-2 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```

```mvn deploy:deploy-file -Dfile=javax.jws-2.0.jar -DgroupId=weblogic -DartifactId=javax.jws -Dversion=2.0 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```

```mvn deploy:deploy-file -Dfile=mwx-common-login-1.0.0.jar -DgroupId=ch.admin.astra.bit -DartifactId=mwx-common-login -Dversion=1.0.0 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```

```mvn deploy:deploy-file -Dfile=sasConnection-9.2.jar -DgroupId=com.sas -DartifactId=sasConnection -Dversion=9.2 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```

```mvn deploy:deploy-file -Dfile=sasSecurity-9.2.jar -DgroupId=com.sas -DartifactId=sasSecurity -Dversion=9.2 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```

```mvn deploy:deploy-file -Dfile=sasCore-9.2.jar -DgroupId=com.sas -DartifactId=sasCore -Dversion=9.2 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```

```mvn deploy:deploy-file -Dfile=wls-api-12.2.1.jar -DgroupId=com.oracle -DartifactId=wls-api -Dversion=12.2.1 -Durl=https://pkgs.dev.azure.com/SWG-SES-OFSPROJECT/OFS/_packaging/ofsdependencies/maven/v1 -DrepositoryId=ofsdependencies```


