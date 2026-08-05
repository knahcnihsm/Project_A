You are a Senior React Frontend Architect with experience building Enterprise Academic ERP Systems.

Build a production-ready Academic ERP Admission Portal using React 19 + TypeScript + Vite.

IMPORTANT

Do NOT redesign the UI.

Do NOT remove any fields.

Do NOT rename any fields.

Do NOT add new fields unless explicitly instructed.

Maintain EXACTLY the same design system throughout the application.

Every page must inherit the same visual language as the Dashboard.

==================================================
DESIGN SYSTEM
==================================================

Use one centralized design system.

Create reusable components for

• Header
• Sidebar
• Breadcrumb
• Footer
• Page Header
• Card
• Data Table
• Search Bar
• Filter Section
• Form Section
• Summary Card
• Upload Component
• Confirmation Dialog
• Success Dialog
• Error Dialog
• Snackbar
• Buttons
• Inputs

Do NOT duplicate UI code.

==================================================
APPLICATION STRUCTURE
==================================================

Use a scalable folder structure.

src/

components/

layout/

forms/

tables/

cards/

dialogs/

uploads/

common/

pages/

dashboard/

admission/

archive/

hooks/

services/

api/

routes/

types/

theme/

utils/

constants/

assets/

App.tsx

==================================================
CODING STANDARDS
==================================================

Use

React Functional Components

TypeScript Interfaces

Strict Type Safety

Reusable Components

Clean Folder Structure

Custom Hooks

No Inline Business Logic

No Duplicate Code

No Hardcoded Values

==================================================
STATE MANAGEMENT
==================================================

Use React Hook Form for all forms.

Use Zod for validation.

Use Context API for global application state.

Persist theme in localStorage.

==================================================
API STRUCTURE
==================================================

Assume backend is Spring Boot REST API.

Create separate service files.

Example

StudentService

AdmissionService

FeeService

CertificateService

ArchiveService

ExportService

ProgramService

DepartmentService

Do not place API calls directly inside components.

==================================================
MASTER DATA
==================================================

Assume the following are fetched dynamically from backend.

Admission Category

Program

Department

District

Bus Routes

Fee Structure

Hostel Fee

Certificate List

Do NOT hardcode these values.

==================================================
FORM BEHAVIOUR
==================================================

Every form should support

Loading State

Saving State

Validation State

Error State

Success State

Unsaved Changes Detection

==================================================
FORM VALIDATION
==================================================

Show validation immediately after blur.

Display helper text.

Highlight invalid fields.

Disable Save button while submitting.

==================================================
USER EXPERIENCE
==================================================

Show Skeleton Loader while loading.

Show Circular Progress during API requests.

Show Snackbar after every successful action.

Show Confirmation Dialog before destructive actions.

Prevent accidental navigation if there are unsaved changes.

==================================================
TABLES
==================================================

Every DataGrid should support

Search

Filter

Sorting

Pagination

Sticky Header

Responsive Layout

Loading State

Empty State

Row Hover

Checkbox Selection

==================================================
DIALOGS
==================================================

Use Material UI Dialog.

Rounded Corners

12px

Consistent Buttons

Responsive

==================================================
FILE UPLOAD
==================================================

Support Drag & Drop.

Show Upload Progress.

Show File Size.

Show File Type.

Allow Replace.

Allow Remove.

Allow Preview.

==================================================
RESPONSIVE DESIGN
==================================================

Desktop

Tablet

Mobile

Collapsible Sidebar

Responsive Forms

Responsive Tables

Responsive Cards

==================================================
PERFORMANCE
==================================================

Lazy load pages.

Memoize reusable components.

Avoid unnecessary re-renders.

==================================================
ACCESSIBILITY
==================================================

Keyboard Navigation

ARIA Labels

Focus Indicators

Screen Reader Friendly

==================================================
CODE QUALITY
==================================================

Production Ready

Maintainable

Reusable

Readable

Well Commented

Enterprise Standard

==================================================
FINAL REQUIREMENT
==================================================

The generated application should look and behave like a real-world University Academic ERP used by institutions such as Anna University, SRM, VIT, or Government Engineering Colleges.

The generated code must be modular, scalable, production-ready, and maintain the exact visual consistency of the Dashboard throughout the entire application.

Only the page content should change between pages.

Everything else (Header, Sidebar, Footer, Colors, Typography, Cards, Tables, Buttons, Forms, Shadows, Spacing, Animations, Layout) must remain identical.

FRONTEND :

Dashboard

Create a modern, premium, professional Academic ERP System UI using React + TypeScript.

IMPORTANT:
Do NOT change the design language.
Maintain the exact theme, spacing, colors, typography, layout, cards, table style, buttons, shadows and UI consistency throughout every page.

The user will provide:

1. College Logo
2. Sidebar Bottom Image
   Use those assets exactly as provided.
   =================================================
   THEME
   =================================================
   Style:
   Modern Government ERP
   Clean
   Minimal
   Premium
   Enterprise
   Academic ERP

Primary Color:
#0D47A1
Secondary Blue:
#1565C0
Accent Blue:
#2979FF
Background:
#F5F8FC
Card:
#FFFFFF
Border:
#E6ECF5
Text:
Primary
#1A2B49
Secondary
#667085
Success
#16A34A
Warning
#F59E0B
Danger
#DC2626
Border Radius:
14px

Card Radius:
16px
Shadow:
0 8px 30px rgba(0,0,0,0.08)
Animation:
Very Smooth
Hover Effects:
Subtle
No Glassmorphism
No Neon
No Dark Corporate Theme
=================================================
LAYOUT
=================================================
Top Header
Left:
College Logo
College Name
Small Tagline
Center:
Admission Portal
Academic ERP System
Right:
Theme Toggle
Settings
Notification
Profile Avatar
Username
Role
Dropdown
Header Height:
90px
Sticky Header

---

Left Sidebar
Width:
300px
Dark Blue Gradient
Rounded Right Edge
Large Navigation Icons
Navigation Items
Dashboard
Add New Admission
Edit Student Details
Archive Students
Export
Export Selected
Export Search Result
Export Student PDF

Bottom:
Display the provided college campus image.
Overlay dark blue gradient.
Bottom Version Text
Academic ERP System
Version 2.x
=================================================
CONTENT
=================================================
Content Area:
White Background
Large Cards
16-20px Padding
Search Bar
Filter Button
Statistics Cards (if needed)
Tables
Forms
Charts
Everything aligned perfectly.

=================================================
TABLE STYLE
=================================================
Rounded Table
White Background
Soft Border
Header:
Dark Blue Text
Uppercase
Bold
Rows:
Hover Light Blue
Checkbox Column
Action Icons
View
Edit
More
Pagination
Modern
Rounded Buttons

=================================================
BUTTONS
=================================================
Primary
Blue Fill
White Text
Rounded
Medium Shadow
Secondary
White
Blue Border
Blue Text
Danger
Red
Success
Green
Hover Animation
Scale 1.02
Transition 250ms

=================================================
FORMS
=================================================
Large Inputs
Rounded
Label Above
Soft Border
Blue Focus
Dropdown
Date Picker
Checkbox
Radio
Stepper
File Upload
Consistent spacing.
=================================================
TYPOGRAPHY
=================================================
Use
Inter
or
Poppins
Heading
700
Body
500
Button
600
=================================================
ICONS
=================================================
Lucide Icons
Outlined
Modern
Consistent Size
=================================================
RESPONSIVE
=================================================
Desktop First
Tablet Support
Mobile Support
Collapsible Sidebar
=================================================
DO NOT
=================================================
Don't redesign.
Don't use random colors.
Don't change spacing.
Don't change typography.
Don't change sidebar style.
Don't use material style.
Don't use bootstrap style.
Don't use glass effect.
Don't use gradients except sidebar.
Don't change overall appearance.
=================================================
IMPORTANT
=================================================
Every newly created page MUST inherit this same UI system.
Same Header
Same Sidebar
Same Footer
Same Buttons
Same Tables
Same Inputs
Same Colors
Same Typography
Same Card Style
Same Shadows
Same Animations
Everything should feel like one complete Academic ERP System.
Only change the content inside the main container.
Never redesign the layout.

Logo & Sidebar Image-ku Placeholder Prompt
The logo and sidebar bottom image will be provided by the user.
Logo:
Replace the default logo with the uploaded college logo while maintaining its aspect ratio and padding.
Sidebar Bottom:
Replace the default campus image with the uploaded image.
Do not crop important parts.
Use object-fit: cover.
Keep the dark blue overlay gradient exactly as the design system.
No other changes should be made to the layout.

ADD NEW ADMISSION PAGE:

Create the "Add New Admission" page for an Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same UI theme, layout, colors, typography, spacing, border radius, shadows, buttons, navbar, sidebar, cards, and design language as the previous Dashboard page.
DO NOT redesign anything.
The Add New Admission page must look like it belongs to the same application.

---

## TOP NAVBAR (Keep Same)

Keep exactly the same navbar.
Left

- College Logo
- RAJIV GANDHI COLLEGE OF ENGINEERING & TECHNOLOGY
- Admission Portal
- Academic ERP System

Right

- Dark Mode Toggle
- Settings Icon
- Admin User Avatar with Dropdown

---

## LEFT SIDEBAR (Same Theme)

Use the same blue gradient sidebar.
Instead of Dashboard menus, display:

← Back
Student Details
Parent Details
Communication
Academic Details
Fee Structure
Certificates Upload
Requirements

• Vertical step navigation
• Current active step highlighted using white background and blue text
• Completed steps should show a blue check icon
• Smooth hover animation
• Sticky sidebar while scrolling
• Keep the college building image at the bottom exactly like the dashboard
• Keep ERP Version section unchanged

---

## CONTENT AREA

Large white card
Rounded corners (12px)
Soft shadow
Same spacing as Dashboard

---

## PAGE TITLE

H1
Add New Admission
H2
Student Details
Below H2 add a small description
"Enter the student's personal information to begin the admission process."

---

## FORM DESIGN

Use Material UI components.
Two-column responsive layout.
Desktop:
2 Columns
Tablet:
2 Columns

Mobile:
1 Column

Gap between fields: 24px
Use Outlined TextField.
Label should float.
Placeholder optional.
Required fields should have \*

---

## FIELDS

1.  Application Number
    (TextField)
2.  Register Number
    (TextField)
3.  Student Name
    (TextField)
4.  Date of Birth
    (Date Picker)
5.  Age
    (Number Field)
    Automatically calculate from DOB.
    Read Only.
6.  Aadhaar Number
    12-digit validation
    Numeric only
7.  Gender
    Dropdown
    Male
    Female
    Transgender
8.  District
    Searchable Dropdown
9.  Nationality
    Dropdown
    Default
    Indian
    Other
10. Caste
    Dropdown
    OC
    BC
    BCM
    MBC
    SC
    SCA
    ST

---

## FORM VALIDATION

Application Number required
Register Number required
Student Name required
DOB required
Aadhaar exactly 12 digits
Age auto calculated
Dropdown validation

---

## BUTTONS

Bottom Right
Previous (Outlined)
Save & Next (Primary Blue)

---

## COLORS

Use exactly the same colors as Dashboard.
Primary Blue
#0B3D91
Royal Blue
#1E5EFF
Sky Blue
#38BDF8
Background
#F7FAFC
White
#FFFFFF
Border
#D8E4F2

---

## ANIMATIONS

Smooth button hover
Input focus animation
Sidebar active animation
Card fade-in

---

## TECH STACK

React 19
TypeScript
Vite
Material UI
React Hook Form
Zod Validation
Use reusable components.
Keep code production-ready.

The final page should visually match the existing Dashboard perfectly, making it feel like the next page of the same Academic ERP system without changing the established theme or layout.

Parent Details Page
Create the "Parent Details" page for the Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same theme, colors, layout, spacing, typography, navbar, sidebar, buttons, cards, shadows, and UI style as the previous "Add New Admission → Student Details" page.
Do NOT redesign anything.

---

## TOP NAVBAR

## Keep exactly the same navbar.

## LEFT SIDEBAR

Keep the same sidebar.
Steps
← Back
✓ Student Details
► Parent Details (Current Step)
Communication
Academic Details
Fee Structure
Certificates Upload
Current step should be highlighted.

---

## CONTENT

H1
Add New Admission
H2
Parent Details
Description
"Enter the parent's information for admission records."

---

## FORM

Responsive two-column layout.
Material UI Outlined Components.
Fields

1. Father Name \*
   (TextField)
2. Father Mobile Number \*
   (TextField)
   10-digit validation
   Numeric only
3. Father Occupation
   (TextField)
4. Annual Income
   (Number Field)
   Prefix ₹
   Positive numbers only

---

## BUTTONS

Bottom Right
Previous
Save & Next

---

## VALIDATION

Father Name required
Mobile required
Exactly 10 digits
Income cannot be negative

---

## TECH

React 19
TypeScript
Vite
Material UI
React Hook Form
Zod
Use reusable components.
Maintain the exact dashboard theme.

Communication Page
Create the "Communication" page for the Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same design system used in all previous pages.
Do NOT change the layout, colors, navbar, sidebar, spacing, typography, buttons, cards, shadows, or theme.

---

## TOP NAVBAR

## Keep unchanged.

## LEFT SIDEBAR

← Back
✓ Student Details
✓ Parent Details
► Communication
Academic Details
Fee Structure
Certificates Upload

---

## CONTENT

H1
Add New Admission
H2
Communication Details
Description
"Enter the student's permanent and communication address."

---

## FORM

## Create two separate cards.

CARD 1
Permanent Address

---

Address
(Multiline TextArea)
PIN Code
Phone Number
Mobile Number
Email Address

---

CARD 2
Communication Address

---

Address
(Multiline TextArea)
PIN Code
Phone Number
Mobile Number
Email Address

---

## ADDITIONAL FEATURE

Below the Permanent Address heading, add a Material UI Checkbox:
☑ Same as Communication Address
When checked:

- Automatically copy all Permanent Address fields into Communication Address.
- Disable editing of Communication Address fields until unchecked.

---

## VALIDATION

PIN Code → 6 digits
Mobile → 10 digits
Phone → Optional
Email → Valid email format
Address required

---

## LAYOUT

Each address section inside a separate white card.
Rounded corners (12px)
Soft shadow
Proper spacing
Responsive layout

---

## BUTTONS

Bottom Right
Previous
Save & Next

---

## TECH

React 19
TypeScript
Vite
Material UI
React Hook Form
Zod
Use reusable components.
The final page must visually match the existing Admission Portal theme exactly.

Academic Details Page
Create the "Academic Details" page for the Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same UI theme, colors, layout, spacing, typography, navbar, sidebar, buttons, cards, shadows, border radius, and design language as the previous pages.
Do NOT redesign anything.

---

## TOP NAVBAR

Keep exactly the same navbar.

- College Logo
- Admission Portal
- Academic ERP System
- Dark Mode Toggle
- Settings
- Admin User

---

## LEFT SIDEBAR

Keep the same sidebar.
Steps
← Back
✓ Student Details
✓ Parent Details
✓ Communication
► Academic Details
Fee Structure
Certificates Upload
Highlight the current step.

---

## CONTENT

H1
Add New Admission
H2
Academic Details
Description
"Enter the student's admission and academic information."

---

## FORM LAYOUT

Create a large white card.
Responsive 2-column layout.
Desktop → 2 Columns
Tablet → 2 Columns
Mobile → 1 Column
Use Material UI Outlined Components.

---

## FIELDS

1. Admission Category \*
   Dropdown
   Options
   • CENTAC
   • Management

---

2. Program \*
   Dropdown
   Options
   • First Year B.Tech
   • Second Year B.Tech (Lateral Entry)
   • PG

---

3. Department \*
   Dropdown
   The department list should automatically change based on the selected Program.
   If Program = First Year B.Tech OR Second Year B.Tech (Lateral Entry)
   Show
   • Computer Science & Engineering (CSE)
   • Electronics & Communication Engineering (ECE)
   • Artificial Intelligence and Machine Learning (AI&ML)
   • Artificial Intelligence and Data Science (AI&DS)
   • Biomedical Engineering (BME)
   • Information Technology (IT)

If Program = PG
Show
• M.Tech Computer Science & Engineering
• M.Tech Wireless Communication
• Master of Business Administration
• Master of Computer Applications

---

4. Batch \*
   (auto generated by department )

---

5. Date of Admission \*
   Material UI Date Picker
   Cannot allow future dates.

---

## VALIDATION

Admission Category required
Program required
Department required
Batch required
Admission Date required

---

## BUTTONS

Bottom Right
Previous
Save & Next

---

## UI

Rounded Card (12px)
Soft Shadow
24px spacing
Floating Labels
Smooth Focus Animation
Responsive Design

---

## TECH STACK

React 19
TypeScript
Vite
Material UI
React Hook Form
Zod Validation
Use reusable components.
The page must look exactly like the existing Admission Portal and feel like the next step of the same workflow.

Qualifying Examination
Create the "Academic Qualification – First Year B.Tech" page for the Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same UI theme, layout, colors, typography, spacing, navbar, sidebar, buttons, shadows, cards, border radius, and design language as the previous pages.
DO NOT redesign anything.
This page must feel like the next step of the same Admission Portal.

---

## TOP NAVBAR

## Keep exactly the same navbar.

## LEFT SIDEBAR

Keep the same sidebar.
Steps
← Back
✓ Student Details
✓ Parent Details
✓ Communication
▼ Academic Details
► Admission Details ✓
► Qualifying Examination (Current)
► HSC Academic Marks

Fee Structure
Certificates Upload

---

• Academic Details should be expandable.
• Highlight "Qualifying Examination".
• Keep all previous completed steps with blue check marks.

---

## CONTENT

H1
Add New Admission
H2
Qualifying Examination
Description
Enter the student's qualifying examination details.

---

SECTION 1
Qualifying Examination

---

Create a white card.
Fields

1. Institution Name \*
2. Institution Place \*
3. Examination Passed \*
   Dropdown
   • HSC
   • CBSE
   • ISC
   • Other
4. Month & Year of Passing \*
   Month-Year Picker
5. SSLC Percentage \*
6. SSLC Register Number \*
7. HSC Percentage \*
8. HSC Register Number \*

---

## BUTTONS

Bottom Right
Previous
Save & Next

---

## UI

Use Material UI
Outlined TextFields
Rounded Card
12px Radius
Soft Shadow
24px spacing
Responsive Layout
Desktop
2 Columns
Mobile
1 Column

---

## VALIDATION

All fields required
Percentage
0–100
Register Number required
Month & Year required

---

## TECH STACK

React 19
TypeScript
Vite
Material UI
React Hook Form
Zod
Reusable Components
Production Ready
The page must visually match the existing Admission Portal exactly.

HSC Academic Marks
Create a single "HSC Marks" page for the Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same UI theme, layout, colors, spacing, typography, navbar, sidebar, buttons, cards, shadows, animations, and design language as the previous pages.
DO NOT redesign anything.
This page is displayed ONLY when
Program = First Year B.Tech

---

## TOP NAVBAR

## Keep exactly the same navbar.

## LEFT SIDEBAR

← Back
✓ Student Details
✓ Parent Details
✓ Communication
▼ Academic Details
✓ Admission Details
✓ Qualifying Examination
► HSC Marks
Fee Structure
Certificates Upload

---

## CONTENT

H1
Add New Admission
H2
HSC Marks
Description
Enter the student's Higher Secondary Examination marks.

---

## LAYOUT

Inside one large white card.
Create two sections.

---

SECTION A
HSC Academic (State Board / CBSE)

---

Add a switch / radio button
Academic
Vocational
When Academic is selected,
show Academic Marks table.
Table Columns
Subject
Month & Year
Maximum Marks
Marks Obtained
Percentage
Subjects
Maths
Physics
Chemistry
Computer Science
Biology
Bio Technology
Automatically calculate Percentage.

---

SECTION B
HSC Vocational

---

When Vocational is selected,
hide Academic table
and display Vocational table.
Columns
Subject
Month & Year
Maximum Marks
Marks Obtained
Percentage
Rows
Related Subject I
Related Subject II
Vocational Subject
Theory
Practical I
Practical II
Automatically calculate Percentage.

---

## AUTO CALCULATIONS

Academic
Percentage
(Marks Obtained / Maximum Marks) × 100
Vocational
Percentage
(Marks Obtained / Maximum Marks) × 100

---

## SUMMARY CARD

Below the table display one summary card.
Automatically display
Total Maximum Marks
Total Marks Obtained
Overall Percentage
Engineering Cut-Off
Cut-Off calculation should automatically use the visible table only.

---

## VALIDATION

Marks cannot exceed Maximum Marks.
Marks cannot be negative.
Month & Year required.

---

## BUTTONS

Previous
Save & Next

---

## UI

Material UI
Responsive
Rounded Cards
12px Radius
Soft Shadow
Sticky Table Header
Hover Rows
Same spacing as the Dashboard.

---

## TECH STACK

React 19

TypeScript

Vite

Material UI

React Hook Form

Zod

Reusable Components

Production Ready

The page must visually match the existing Admission Portal exactly.

II Year B.Tech (Lateral Entry)
Create the "Diploma Details" page for the Academic ERP Admission Portal.
IMORTANT:
Maintain EXACTLY the same UI theme, layout, colors, typography, spacing, navbar, sidebar, buttons, cards, shadows, border radius, and design language as the previous Admission Portal pages.
DO NOT redesign anything.
This page should ONLY appear when:
Program = Second Year B.Tech (Lateral Entry)

---

## TOP NAVBAR

## Keep exactly the same navbar.

## LEFT SIDEBAR

← Back
✓ Student Details
✓ Parent Details
✓ Communication
▼ Academic Details
✓ Admission Details
► Diploma Details (Current)
Fee Structure
Certificates Upload

• Academic Details should be expandable.
• Highlight Diploma Details.
• Completed steps should display blue check icons.
• Keep the sidebar design identical to the dashboard.

---

## CONTENT

H1
Add New Admission
H2
Diploma Details
Description
Enter the student's Diploma qualification details for lateral entry admission.

---

## FORM LAYOUT

Create one large white card.
At the top of the card include the following fields.
Diploma Course _
(TextField)
Institution Name _
(TextField)
Board _
(Dropdown)
Options
• DOTE
• AICTE
• Autonomous
• Other
Month & Year of Passing _
(Month-Year Picker)
Register Number \*
(TextField)

---

## DIPLOMA MARKS TABLE

Below the form create a Material UI table.
Columns
Diploma Course
Second Year %
Third Year %
Aggregate of II & III Year %
Board
Rows
One editable row.
Fields
Diploma Course
(TextField)
Second Year %
(Number)
Third Year %
(Number)
Aggregate %
(Read Only)
Automatically calculate
(Second Year % + Third Year %) ÷ 2
Board
(Read Only)
Automatically populate from the selected Board above.

---

## SUMMARY CARD

Below the table create a summary card.
Display
Second Year Percentage
Third Year Percentage
Aggregate Percentage
All values should update automatically.

---

## VALIDATION

Percentages
0–100
Aggregate
Auto calculated
Register Number required
Institution Name required
Board required
Passing Year required

---

## BUTTONS

Previous
Save & Next

---

## UI

Material UI
Responsive Layout
Rounded Cards
12px Radius
Soft Shadow
Sticky Table Header
Hover Rows
Professional spacing

---

## TECH STACK

Same

PG
Create the "PG Qualification Details" page for the Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same UI theme, layout, colors, typography, spacing, navbar, sidebar, buttons, cards, shadows, border radius, animations, and design language as all previous Admission Portal pages.
DO NOT redesign anything.
This page should ONLY appear when:
Program = M.Tech (CSE)
OR
Program = M.Tech (Wireless Communication)
OR
Program = MBA
OR
Program = MCA

---

## TOP NAVBAR

## Keep exactly the same navbar.

## LEFT SIDEBAR

← Back
✓ Student Details
✓ Parent Details
✓ Communication
▼ Academic Details
✓ Admission Details
► PG Qualification Details (Current)
Fee Structure
Certificates Upload

• Academic Details should be expandable.
• Highlight "PG Qualification Details".
• Completed steps should display blue check icons.
• Keep the sidebar identical to the rest of the application.

---

## CONTENT

H1
Add New Admission
H2
PG Qualification Details
Description
Enter the qualifying degree details for postgraduate admission.

---

## QUALIFYING EXAMINATION

Create one large white card.
Use a responsive two-column layout.
Fields

1. University Name \*
   (TextField)
2. University Place \*
   (TextField)
3. Institution Name \*
   (TextField)
4. Institution Place \*
   (TextField)
5. Examination Passed \*
   (TextField)
   Examples
   • B.E.
   • B.Tech
   • B.Sc.
   • BCA
   • B.Com.
   • Other
6. Month & Year of Passing \*
   (Month-Year Picker)
7. Total Percentage of Marks \*
   (Number Field)
   Validation
   0–100
8. Main Subject Percentage \*
   (Number Field)
   Validation
   0–100
9. Degree Registration Number \*
   (TextField)

---

## SUMMARY CARD

Below the form display a summary card.
Show
• Examination Passed
• Overall Percentage
• Main Subject Percentage
• Passing Year
The summary should update automatically as the user enters data.

---

## VALIDATION

University Name required
Institution Name required
Examination Passed required
Passing Month & Year required
Overall Percentage must be between 0 and 100
Main Subject Percentage must be between 0 and 100
Degree Registration Number required

---

## BUTTONS

Previous
Save & Next

---

## UI

Material UI
Outlined TextFields
Rounded Cards (12px)
Soft Shadow
24px spacing
Responsive Layout
Desktop → 2 Columns
Tablet → 2 Columns
Mobile → 1 Column
Floating Labels
Smooth Focus Animation

---

## TECH STACK

Same

The page must visually match the existing Admission Portal and seamlessly fit into the same workflow without changing the established design.

FEE Structure
Create the "Fee Structure" page for the Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same UI theme, layout, colors, typography, spacing, navbar, sidebar, buttons, cards, shadows, border radius, animations, and design language as all previous Admission Portal pages.
DO NOT redesign anything.

---

## TOP NAVBAR

## Keep exactly the same navbar.

## LEFT SIDEBAR

Keep the same sidebar and step navigation.
Example
← Back
✓ Student Details
✓ Parent Details
✓ Communication
✓ Academic Details
► Fee Structure (Current)
Certificates Upload
Highlight the current step.

---

## CONTENT

H1
Add New Admission
H2
Fee Structure
Description
Assign the student's tuition fee and optional hostel/bus fee.

---

## LAYOUT

Create one large white card.
Responsive 2-column layout.
Desktop
2 Columns
Tablet
2 Columns
Mobile
1 Column
Use Material UI Outlined Components.

---

## FIELDS

1. Cut-Off Mark
   Read Only
   Automatically display the calculated Cut-Off based on Academic Details.

---

2. Tuition Fee Per Year \*
   Read Only
   Automatically populate according to the selected Admission Category and Program.
   Examples
   CENTAC → ₹75,000
   Management → Fetch from Fee Structure Master

---

3. Total Tuition Fee
   Read Only
   Automatically calculate.
   Formula
   Tuition Fee Per Year × Course Duration
   Examples
   First Year B.Tech (4 Years)
   ₹75,000 × 4 = ₹3,00,000
   Second Year B.Tech (Lateral Entry - 3 Years)
   Fee × 3
   MBA (2 Years)
   Fee × 2
   MCA (2 Years)
   Fee × 2
   M.Tech (2 Years)
   Fee × 2

---

4. Bus Fee
   Dropdown
   Options
   • Not Applicable
   • Select Bus Route
   When a route is selected,
   Automatically display Bus Fee from the Bus Route Master.
   Read Only

---

5. Hostel Fee
   Switch
   No
   Yes
   If Yes
   Automatically display Hostel Fee from Hostel Master.
   Read Only
   If No
   Display ₹0

---

## SUMMARY CARD

Below the form create a summary card.
Display
Cut-Off Mark
Tuition Fee Per Year
Total Tuition Fee
Bus Fee
Hostel Fee
Grand Total Fee
Grand Total should automatically calculate.
Formula
Total Tuition Fee

- Bus Fee
- ## Hostel Fee
  ## AUTO LOGIC
  All values should be fetched automatically.
  User should NOT manually type
  Cut-Off
  Tuition Fee
  Total Tuition Fee
  Bus Fee Amount
  Hostel Fee Amount
  Only Bus Route selection and Hostel Yes/No are editable.
  ***
  ## VALIDATION
  Bus Route required only if Bus is selected.
  Hostel Fee shown only when Hostel = Yes.
  Grand Total updates instantly.
  ***
  ## BUTTONS
  Bottom Right
  Previous
  Save & Next
  ***
  ## UI
  Material UI
  Rounded Cards
  12px Radius
  Soft Shadow
  Floating Labels
  Responsive Design
  Professional spacing
  Same visual style as the Dashboard and previous pages.
  ***
  ## TECH STACK
  React 19
  TypeScript
  Vite
  Material UI
  React Hook Form
  Zod Validation
  Reusable Components
  Production Ready
  The page must visually match the existing Admission Portal exactly and continue the same workflow without changing the established design.

Upload Certificate
Create the "Certificates Upload" page for the Academic ERP Admission Portal.
IMPORTANT:
Maintain EXACTLY the same UI theme, colors, layout, typography, spacing, navbar, sidebar, cards, shadows, buttons, border radius, and animations as all previous Admission Portal pages.
DO NOT redesign anything.

---

## TOP NAVBAR

## Keep exactly the same navbar.

## LEFT SIDEBAR

← Back
✓ Student Details
✓ Parent Details
✓ Communication
✓ Academic Details
✓ Fee Structure
► Certificates Upload (Current)

---

## CONTENT

H1
Add New Admission
H2
Certificates Upload
Description
Verify and upload the student's admission documents.

---

## LAYOUT

Create one large white card.
Inside the card display a responsive table.
Each row represents one certificate.

---

## TABLE COLUMNS

✓ Received
Certificate Name
Upload File
Preview

---

## CERTIFICATES

□ Provisional Allotment Order
□ Special Category Certificate
□ Provisional Certificate
□ Undertaking Form
□ Mark Sheet
□ Degree Certificate
□ Residence Certificate
□ Transfer Certificate
□ Proof of Age
□ Community Certificate
□ Conduct Certificate
□ Aadhaar Card

---

## FUNCTIONALITY

Each row should contain

1.  Material UI Checkbox
    Received
2.  Certificate Name
    (Read Only)
3.  Upload Button
    Accept
    PDF
    JPG
    JPEG
    PNG
    Maximum Size
    10 MB
4.  Preview Button
    Opens uploaded document in a modal dialog.

---

## AUTO LOGIC

Checkbox checked
↓
Upload button becomes enabled.
Checkbox unchecked
↓
Upload disabled.
If a file is uploaded
Display
✔ Uploaded
Green success icon
Allow
Replace File
Remove File
Preview File

---

## SUMMARY CARD

Below the table display
Certificates Received
Certificates Uploaded
Pending Certificates
Progress Bar
Example
8 / 12 Uploaded

---

## VALIDATION

Allow only
PDF
JPG
JPEG
PNG
Maximum
10 MB
Do not allow upload unless the checkbox is checked.

---

## BUTTONS

Previous
Save Admission

---

## UI

Material UI Table

Rounded Card

12px Radius

Soft Shadow

Sticky Header

Hover Rows

Responsive

Professional spacing

Green success chips

Blue upload buttons

---

## TECH STACK

React 19

TypeScript

Vite

Material UI

React Hook Form

Zod Validation

React Dropzone (or Material UI File Upload)

Reusable Components

Production Ready

The page must visually match the existing Admission Portal exactly.

Edit Student Details
Create the "Edit Student Details" functionality for the Academic ERP Admission Portal.
IMPORTANT
Maintain EXACTLY the same UI theme, colors, layout, typography, spacing, navbar, sidebar, cards, shadows, buttons, and design language as the existing Admission Portal.
DO NOT redesign anything.
The Edit Student Details feature should support TWO workflows.
====================================================
WORKFLOW 1
Single Student Edit
====================================================
From the Student List page,
when exactly ONE student checkbox is selected,
and the user clicks
Edit Student Details
navigate to
/EditStudent
Do NOT create a new UI.
Reuse the existing "Add New Admission" page.

---

Change only
H1
Edit Student Details
instead of
Add New Admission

---

Everything else should remain exactly the same.
Student Details
Parent Details
Communication
Academic Details
Fee Structure
Certificates Upload

---

AUTO LOAD
Fetch all student details from the database using
Student ID
Populate every form automatically.
This includes
Student Details
Parent Details
Communication
Academic Details
Fee Structure
Certificates
The user can edit any field.

---

Buttons
Previous
Update Student

---

When Update Student is clicked
Validate all forms
Update the database
Display
Student Details Updated Successfully
====================================================
WORKFLOW 2
Bulk Update
====================================================
If NO checkbox is selected
and the user clicks
Edit Student Details
DO NOT navigate.
Instead
Open a centered Material UI Modal Dialog.

---

Modal Title
Bulk Student Update

---

Description
Upload an Excel file to update existing student records in bulk.
Only existing students will be updated based on their Register Number or Application Number.

---

Inside the Modal
Download Sample Excel Template
(Button)

---

Drag & Drop Upload Area
OR
Choose Excel File
Accepted Files
.xlsx
.xls
Maximum Size
20 MB

---

After Upload
Display
Uploaded File Name
Total Records Found
Valid Records
Invalid Records
Duplicate Records

---

Validation Summary
Example
Total Records : 150
Valid : 146
Invalid : 3
Duplicate : 1

---

Buttons
Cancel
Process Excel

---

When Process Excel is clicked
Read the Excel file.
For every row
Search existing student using
Register Number
or
Application Number
If found
Update all corresponding fields.
If not found
Ignore the row.
At the end display
Bulk Update Completed Successfully
Example
146 students updated successfully.
3 invalid records skipped.
1 duplicate skipped.

---

====================================================
UI
====================================================
Material UI Dialog
Rounded Corners
12px Radius
Soft Shadow
Responsive
Progress Indicator while processing
Success Snackbar
Error Snackbar
====================================================
TECH STACK
====================================================
SAME
Maintain the exact Admission Portal theme and user experience throughout the workflow.

Archived Student details
Create the "Archived Student Details" page for the Academic ERP Admission Portal.
IMPORTANT
Maintain EXACTLY the same UI theme, colors, layout, typography, spacing, navbar, sidebar, buttons, cards, shadows, border radius, icons, animations, and design language as the existing Student List page.
DO NOT redesign anything.
====================================================
PURPOSE
====================================================
This page displays students who have been archived (soft deleted).
Archived students are NOT permanently deleted.
All data is retrieved from the Archive table in the database.
====================================================
TOP NAVBAR
====================================================
Keep exactly the same navbar.
====================================================
LEFT SIDEBAR
===================================================
Dashboard
Add New Admission
Edit Student Details
► Archived Student Details
Export
Export Selected Student Excel
Export Searched Students
Export Student Details PDF
Highlight Archived Student Details.
====================================================
CONTENT
====================================================
H1
Archived Student Details
Description
View students who have been archived from the admission portal.
====================================================
SEARCH & FILTER
===================================================
Search
Search by
Student Name
Application Number
Register Number
Department
Reason

---

Filter
Department
Admission Category
Program
Batch
Reason
Date Deleted
====================================================
TABLE
====================================================
Material UI DataGrid
Columns
Application Number
Register Number
Student Name
Department
Program
Admission Category
Reason
Deleted By
Deleted Date
Actions
====================================================
REASON
====================================================
Examples
Transfer Certificate (TC)
Admission Cancelled
Duplicate Admission
Course Discontinued
Student Request
Management Decision
Other
====================================================
ACTIONS
====================================================
View Details
Restore Student
More Menu
====================================================
VIEW DETAILS
====================================================
When View Details is clicked
Open the same Admission Details page used for Add/Edit Student.
Change only
H1
Archived Student Details
All sections remain the same.
Student Details
Parent Details
Communication
Academic Details
Fee Structure
Certificates Upload
All fields should be
Read Only
Retrieve all information from the Archive database.
====================================================
RESTORE
====================================================
When Restore Student is clicked
Display confirmation dialog.
Restore this student to the active admission list?
Buttons
Cancel
Restore
If confirmed
Move all archived data back to the active Student tables.
Remove it from Archive.
Display
Student Restored Successfully
====================================================
MULTIPLE RESTORE
====================================================
If multiple checkboxes are selected
Enable
Restore Selected
Restore every selected archived student.
====================================================
EMPTY STATE
====================================================
If no archived students exist
Display
Archive Empty
No archived student records found.
====================================================
UI
====================================================
Sticky Header
Hover Rows
Rounded Cards
12px Radius
Soft Shadow
Responsive
Professional spacing
Same dashboard theme.
====================================================
TECH STACK
====================================================
same
====================================================
DATABASE
====================================================
Fetch data from
Archived_Students
Retrieve
Student Details
Parent Details
Communication
Academic Details
Fee Structure
Certificates
Archive Information
Reason
Deleted By
Deleted Date
Do NOT permanently delete any data.
Use soft delete architecture.
Maintain the exact Admission Portal theme and user experience.
