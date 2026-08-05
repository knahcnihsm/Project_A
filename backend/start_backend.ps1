$jars = Get-ChildItem -Path "C:\Users\MOHANKUMAR\.m2\repository" -Filter "*.jar" -Recurse | Where-Object { $_.FullName -notmatch "slf4j-api[\\\/]1\." } | Select-Object -ExpandProperty FullName
$cp = ($jars -join ";") + ";C:\Users\MOHANKUMAR\Documents\Project_A\backend\target\classes"
java -cp $cp com.rgcet.admission.AdmissionApplication
