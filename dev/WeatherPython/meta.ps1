$raw = (Get-Content ~\Documents\SmartAdapt\WeatherPython\ieq.lvm)[-1]
$object = $raw -replace '\s+',','
[string[]]$array = $object.Split(',')
$object = "{{ `"temperature`": {0}, `"meanTemp`": {1}, `"CO2`": {2}, `"humidity`": {3} }}" -f $array[3], $array[7], $array[8], $array[9]
$object = $object | ConvertFrom-Json
$object | ConvertTo-Json | Out-File C:\dev\WeatherPython\data.json

py weather.py

exit