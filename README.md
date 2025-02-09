# Bilkent-University-TA-Management-System

## TA Management System

## Overview
The “TA Management System” is a web application developed as part of the CS319 - Fall 2025 project at Bilkent University. It is designed to streamline the workload management and scheduling of Teaching Assistants (TAs) by efficiently assigning TA duties, exam proctoring tasks, and reporting workload statistics.

This system aims to “reduce administrative workload” by automating proctoring assignments and balancing workloads among TAs, ensuring fairness and efficiency.

## Core Features
TA Duties Management
TAs perform a variety of tasks, including:
- Assisting with assigned courses
- Conducting lab sessions
- Grading assignments and exams
- Leading recitation sessions
- Holding office hours
- Exam proctoring duties
  TAs have their own schedule to see their courses and additional changes during the semester.

The system ensures that “TA workloads are balanced” by tracking their completed and approved (by the instructor) tasks and prioritizing proctoring assignments for TAs with lower workloads.

#### Proctoring Assignment Workflow
Exam proctoring duties are assigned with priority to TAs with the least workload. This ensures an even distribution of responsibilities.

Proctoring assignments can be done in two ways:
The information on how many TAs are needed should be entered by the authorized staff and choose whether the assignments should be automatic or manual.
1. Automatic Assignment:  
   - The system prioritizes assigning **TAs of that course** first.
   - If additional proctors are needed(if the required number of TAs can not be taken from that course), **other TAs from the department** are assigned to the Dean’s office.
   - TAs who **have no proctoring duties on the day before/after** the exam receive priority.

Special Restrictions Used to Filter TAs for Proctoring Assignments
The course assigned to the TA is MS/PhD level and the assistant meets that condition
TA taking that course as a student can not be a proctor
TA has an exam as the student during the exam time/date
TA is on leave near the exam time
2. Manual Assignment:  
   - Faculty or authorized staff can **manually assign** TAs based on availability and exam requirements.
   - Options of **exam requirements** by which TAs will be selected for proctoring
   - The system **tracks assignment swaps** to prevent repeated workload issues.

#### TA Duty Workflow
- TAs log their completed tasks (e.g., grading, lab sessions, office hours) and duration time.
- The course instructor receives a notification and can approve/reject the task posted by the TA.
- Once approved, the recorded hours needed to complete the task are added to the TA's total workload.

#### TA Leave Management
- TAs can send leave of absence(medical issues, conferences, or personal leave) request.
- Department staff must approve/reject the request.
- Approved request ensures that TAs are not assigned to the proctoring duties during those dates.

#### Proctor Swap System
- TAs can swap proctoring duties among themselves and the system sends the notification to parties about changes.
- The system updates workload records accordingly.
- Department staff can also reassign proctoring duties manually.

#### Exam Scheduling and Classroom Distribution
- Faculty or staff can generate student distribution lists for exams.
- The system allows for alphabetical or randomized classroom seating arrangements.

#### Dean’s Office Oversight
- The Dean’s Office can assign proctoring duties for exams scheduled by themselves across multiple departments.
- The Dean’s Office can choose departments TAs will be selected for proctoring duties scheduled by themselves.
Workload Limits
- Cap can be set on the maximum hours a TA can work in a semester and the assigning process should be set not to exceed this workload.
- This prevents TAs from exceeding reasonable workloads.

#### Data Management
#### Stored Information
- Students Table (Student ID, Name, TA status, MS/PhD status)
- Staff Table (Personnel ID, Name, Role)
- Courses Table (Course offerings, Instructor details)
- Classrooms Table (Room numbers, capacities)
- TA Assignments (Completed tasks, proctoring history)
- System Logs (All activities: logins, assignments, swaps, etc.)

#### Import & Export Functionality
- Data (students, faculty, courses) can be imported via Excel spreadsheets. Different types of reports can be downloaded by users considering their roles.
- The system provides reports on:
  - Total proctoring per semester
  - Total TA workload per course
  - Exam distribution list of students for each classroom
    
#### Technology Stack
- Backend: not decided
- Frontend: not decided
- Database: MySQL
- Hosting: Apache2 on Linux servers

#### Actors in the System
- Teaching Assistant (TA) 
- Faculty Member
- Department Staff 
- Department Chair 
- Dean’s Office 
- Admin

#### Key Objectives & Benefits
* Automated workload balancing for TAs  
* Fair assignment of proctoring duties  
* Efficient tracking of completed TA tasks 
* Easier scheduling & exam proctoring management 
* Comprehensive data reporting for administrators 

This system ensures efficient TA management, minimizing manual workload for faculty while improving fairness and transparency in TA duty assignments.

#### Group Members
* Emiralp İlgen 		22203114
* Perhat Amanlyyev 	22201007
* İlmay Taş 		   22201715
* Anıl Keskin 		22201915
* Simay Uygur 		22203328

