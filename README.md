[![Build Status](https://app.travis-ci.com/magujun/javafx-minepark.svg?branch=main)](https://app.travis-ci.com/magujun/MineparkOnline)

# MineparkOnline

<h3 align="center">
     <img width="75px" src="https://user-images.githubusercontent.com/75567460/159811876-e8556b1d-fb47-4b57-a884-4d5294c21f29.png">
    <img width="250px" src="https://user-images.githubusercontent.com/75567460/159811559-b59300f5-4aba-4d1d-b44d-c26b04501986.png">
        <img width="75px" src="https://user-images.githubusercontent.com/75567460/159811641-962e9d84-8160-4969-9328-6d50788359a8.png">
    <br><br>
    <p align="center">
      <a href="#-technology">Technology</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
      <a href="#-setup">Setup</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
      <a href="#-contribute">Contribute</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
      <a href="#-license">License</a>
  </p>
</h3>

## 🔖 About

[![GitHub forks](https://img.shields.io/github/forks/magujun/MineparkOnline?style=social)](https://github.com/magujun/MineparkOnline/network/members/)
[![GitHub stars](https://img.shields.io/github/stars/magujun/MineparkOnline?style=social)](https://github.com/magujun/MineparkOnline/stargazers/)

A <strong>JavaFX</strong> multiplayer implementation of the classic Minesweeper game with a **[South Park™](https://www.southparkstudios.com/)** theme.

## 🚀 Technology

This project has been developed and tested with the following technologies:

- [Java](https://www.java.com/en/) :: Oracle Java™ Language | OpenJDK JRE
- [JavaFX](https://openjfx.io/) :: JavaFX Framework | OpenJFX SDK (>= 17)
- [Maven](https://maven.apache.org) :: Apache Maven Plug-in (>= 3.8.4)
- [...Work in progress](https://github.com/magujun/MineparkOnline)

## ⤵ Setup

These instructions will take you to a copy of the project running on your local machine for testing and development purposes.

```bash
    - git clone https://github.com/magujun/MineparkOnline.git
    - cd MineparkOnline
```    

**NOTE:** You will need to download the javaFX libs and Apache Maven to run the application.

The following commands are an example for a GNU/Linux x86_64 environment (Debian-based).
```    
    - sudo apt-get install unzip
    - mkdir tmp/
    - curl -L https://download2.gluonhq.com/openjfx/18/openjfx-18_linux-x64_bin-sdk.zip > tmp/openjfx-18_linux-x64_bin-sdk.zip
    - unzip tmp/openjfx-18_linux-x64_bin-sdk.zip -d tmp/
 ```
 *For other platforms, please check [openJFX.io](https://gluonhq.com/products/javafx/) and download the openjfx SDK for your system.*
 
  **COMPILE**
 ```   
    - mkdir bin/
    - javac -d bin --module-path tmp/javafx-sdk-18/lib --add-modules=javafx.controls,javafx.media,javafx.graphics -classpath bin:tmp/javafx-sdk-18/lib/javafx-swt.jar:tmp/javafx-sdk-18/lib/javafx.base.jar:tmp/javafx-sdk-18/lib/javafx.controls.jar:tmp/javafx-sdk-18/lib/javafx.graphics.jar:tmp/javafx-sdk-18/lib/javafx.media.jar:tmp/javafx-sdk-18/lib/javafx.swing.jar:tmp/javafx-sdk-18/lib/javafx.web.jar src/*
```

**RUN**
```
    - java --module-path tmp/javafx-sdk-18/lib --add-modules=javafx.controls,javafx.media,javafx.graphics -classpath bin:tmp/javafx-sdk-18/lib/javafx-swt.jar:tmp/javafx-sdk-18/lib/javafx.base.jar:tmp/javafx-sdk-18/lib/javafx.controls.jar:tmp/javafx-sdk-18/lib/javafx.graphics.jar:tmp/javafx-sdk-18/lib/javafx.media.jar:tmp/javafx-sdk-18/lib/javafx.swing.jar:tmp/javafx-sdk-18/lib/javafx.web.jar Minepark
```

## Using Maven
Install [Maven](https://maven.apache.org/install.html) 3.9.1 and run from project root

**MAVEN Compile & Run**
- MineparkOnline (Server) ```mvn exec:java```
- Minepark (Game) ```mvn javafx:run```

**MAVEN Compile & Package**
- MineparkOnline (Server) ```mvn jlink:compile jpackage:jpackage```
- Minepark (Game) ```mvn jlink:compile jpackage:jpackage```

## 🎓 Who taught?

The Java and JavaFX courses that led me to develop this project are part of Okanagan College's Computer Information Systems.

Course Instructors:

**[Sarah Foss](https://github.com/sarahfoss)**  COSC121 :: Computer Programming II

**[Daniel Ling](https://www.okanagan.bc.ca/ling-daniel)** COSC318 :: Network Programming

## 🤔 Contribute

- Fork this repository;
- Create a branch with your feature: `git checkout -b my-feature`;
- Commit your changes: `git commit -m 'feat: My new feature'`;
- Push to your branch: `git push origin my-feature`.

After the merge of your pull request is done, you can delete your branch.

## 📝 License

This project is under the MIT license.<br/>
See the [LICENSE](LICENSE) file for more details.

---

<h4 align="center">
  Done with ❤ by <a href="https://www.linkedin.com/in/marcelo-guimaraes-junior/" target="_blank">Marcelo Guimarães Junior.</a><br/>
</h4>
