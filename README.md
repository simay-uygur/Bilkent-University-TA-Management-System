# Bilkent-University-TA-Management-System

## *TA Management System*
### *Overview*
The *TA Management System* is a web application developed as part of the CS319 - Fall 2024 project at *Bilkent University. It is designed to streamline the workload management and scheduling of **Teaching Assistants (TAs)* by efficiently assigning *TA duties, exam proctoring tasks, and reporting workload statistics*.

This system aims to *reduce administrative workload* by automating proctoring assignments and balancing workloads among TAs, ensuring fairness and efficiency.

---

## *Core Features*
### *TA Duties Management*
TAs perform a variety of tasks, including:
•⁠  ⁠Assisting with assigned courses
•⁠  ⁠Conducting lab sessions
•⁠  ⁠Grading assignments and exams
•⁠  ⁠Leading recitation sessions
•⁠  ⁠Holding office hours
•⁠  ⁠Exam proctoring duties

The system ensures that *TA workloads are balanced* by tracking their completed tasks and prioritizing proctoring assignments for TAs with *lower workloads*.

### *Proctoring Assignment Workflow*
Exam proctoring duties are assigned with priority given to TAs with the least workload. This ensures an **even distribution of responsibilities*.

*Proctoring assignments can be done in two ways:*
1.⁠ ⁠*Automatic Assignment*:  
   - The system prioritizes assigning *TAs of that course* first.
   - If additional proctors are needed, *other TAs from the department* are assigned.
   - TAs who *have no proctoring duties on the day before/after* the exam receive priority.

2.⁠ ⁠*Manual Assignment*:  
   - Faculty or authorized staff can *manually assign* TAs based on availability.
   - Prompts are displayed to suggest available TAs and highlight workload distribution.
   - The system *tracks assignment swaps* to prevent repeated workload issues.

### *TA Duty Workflow*
•⁠  ⁠TAs *log* their completed tasks (e.g., grading, lab sessions, office hours).
•⁠  ⁠The *course instructor* receives a notification and can approve/reject the task.
•⁠  ⁠Once approved, the recorded hours are *added to the TA's total workload*.

### *TA Leave Management*
•⁠  ⁠TAs can request *leave of absence* for reasons such as medical issues, conferences, or personal leave.
•⁠  ⁠*Department staff* must approve/reject the request.
•⁠  ⁠Approved leave ensures that *TAs are not assigned* to proctoring duties during those dates.

### *Proctor Swap System*
•⁠  ⁠*TAs can swap* proctoring duties among themselves.
•⁠  ⁠The system updates workload records accordingly.
•⁠  ⁠*Department staff* can also reassign proctoring duties manually.

### *Exam Scheduling and Classroom Distribution*
•⁠  ⁠Faculty or staff can generate *student distribution lists* for exams.
•⁠  ⁠The system allows for *alphabetical or randomized* classroom seating arrangements.

### *Dean’s Office Oversight*
•⁠  ⁠The *Dean’s Office* can assign proctoring duties across *multiple departments* when required.
•⁠  ⁠Departments included in the proctoring pool can be selected dynamically.

### *Workload Limits*
•⁠  ⁠*Caps* can be set on *maximum hours* a TA can work in a semester.
•⁠  ⁠This prevents TAs from exceeding reasonable workloads.

---

## *Data Management*
### *Stored Information*
•⁠  ⁠*Students Table* (Student ID, Name, TA status, MS/PhD status)
•⁠  ⁠*Staff Table* (Personnel ID, Name, Role)
•⁠  ⁠*Courses Table* (Course offerings, Instructor details)
•⁠  ⁠*Classrooms Table* (Room numbers, capacities)
•⁠  ⁠*TA Assignments* (Completed tasks, proctoring history)
•⁠  ⁠*System Logs* (All activities: logins, assignments, swaps, etc.)

### *Import & Export Functionality*
•⁠  ⁠Data (students, faculty, courses) can be *imported via Excel spreadsheets*.
•⁠  ⁠The system provides *reports* on:
  - *Total proctoring per semester*
  - *Total TA workload per course*
  - *Exam assignment statistics*

---

## *Technology Stack*
•⁠  ⁠*Backend:* Spring Boot
•⁠  ⁠*Frontend:* React
•⁠  ⁠*Database:* MySQL
•⁠  ⁠*Hosting:* Apache2 on Linux servers

---

## *Actors in the System*
1.⁠ ⁠*Teaching Assistants (TAs)*
2.⁠ ⁠*Course Instructors*
3.⁠ ⁠*Department Staff & Coordinators*
4.⁠ ⁠*Dean’s Office Administrators*

---

## *Key Objectives & Benefits*
✅ *Automated workload balancing* for TAs  
✅ *Fair assignment of proctoring duties*  
✅ *Efficient tracking of completed TA tasks*  
✅ *Easier scheduling & exam proctoring management*  
✅ *Comprehensive data reporting for administrators*  

This system ensures *efficient TA management, minimizing **manual workload* for faculty while improving *fairness and transparency* in TA duty assignments.

---
## Group Members

Emiralp İlgen 22203114
Perhat Amanlyyev 22201007
İlmay Taş 22201715
Anıl Keskin 22201915
Simay Uygur 22203328

