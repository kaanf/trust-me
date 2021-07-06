# 📓 Guideline of Trust Me
Trust Me project is properly designed to be used with Android devices as a mobile application, for taking instant attendancy in the classrooms. Students will login into application, and enter to a classroom with submitting their face. Face recognition algorithm will examine the submitted photograph while comparing it to students’ photograph exist in database, and take the attendance of student. 

<center>
    <img src="/img/img_1.png" alt="drawing" width="400"/> 
</center>

## User Interfaces
Splash Screen           |  Login Screen            |  Face Recognition Screen
:-------------------------:|:-------------------------:|:-------------------------:
![](/img/ui_1.jpg)  |  ![](/img/ui_2.jpg)  |  ![](/img/ui_3.jpg)


## Software Methodology
The waterfall method was used in the software development life cycle of this project. Because the dates of the phases in the project development process have already been determined, this method is used. 
The waterfall method is the most common software development methodology and serves as the foundation for all other models. The steps of analysis, design, implementation, and testing are generally carried out in four stages. 

## Software Architecture
The Trust Me App based on many external frameworks and libraries such as Firebase (ML, Real Time Database, Authentication, Storage Libraries), GSON, Glide, TensorFlow. The system works with some of these frameworks and libraries in Android Studio (Java). System uses Firebase DB for storage all information about users including face pictures of them. Also, Firebase used for server connections.

<center>
    <img src="/img/img_2.png" alt="drawing" width="600"/> 
</center>

## Firebase Architecture
The Trust Me’s firebase architecture consists of several components. These are Firebase Authentication, Firebase Functions, Firebase Database, Firebase Storage. 

<center>
    <img src="/img/img_3.jpg" alt="drawing" width="300"/> 
</center>

## Class Diagram
<center>
    <img src="/img/img_4.png" style="transform:rotate(270deg);" alt="drawing" width="500"/> 
</center>

## Sequence Diagrams
### Login Screen
<center>
    <img src="/img/img_5.png" alt="drawing" width="500"/> 
</center>

### Mail Screen
<center>
    <img src="/img/img_6.png" alt="drawing" width="500"/> 
</center>

### Details Screen
<center>
    <img src="/img/img_7.png" alt="drawing" width="500"/> 
</center>

### Phone Screen
<center>
    <img src="/img/img_8.png" alt="drawing" width="500"/> 
</center>

### Face Recognition Screen
<center>
    <img src="/img/img_9.png" alt="drawing" width="500"/> 
</center>

