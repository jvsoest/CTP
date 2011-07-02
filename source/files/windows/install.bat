CTP.exe ^
 //IS//CTP ^
 --Install="${home}"\windows\CTP.exe ^
 --Description="RSNA CTP Service" ^
 --Startup="auto" ^
 --Jvm=auto ^
 --JvmOptions=-Djava.ext.dirs="'${ext}'" ^
 --StartMode=jvm ^
 --JvmMs=128 ^
 --JvmMx=512 ^
 --StartPath="${home}" ^
 --StartClass=org.rsna.ctp.ClinicalTrialProcessor ^
 --StartMethod=startService ^
 --StartParams=start ^
 --StopMode=jvm ^
 --StopPath="${home}" ^
 --StopClass=org.rsna.ctp.ClinicalTrialProcessor ^
 --StopMethod=stopService ^
 --StopParams=stop ^
 --LogPath="${home}"\logs ^
 --StdOutput=auto ^
 --StdError=auto
