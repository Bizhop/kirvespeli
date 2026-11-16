#!/bin/bash
ls -1 *.svg > lines.txt
rm svgImages.js

pattern="^([0-9])"

while read -r line; do
  abbr=${line%.*}
  image=$abbr
  
  if [[ $abbr =~ $pattern ]];
  then 
    abbr=x$abbr 
  fi
  
  echo "import $abbr from \"./$image.svg\"" >> svgImages.js
done < lines.txt

echo "" >> svgImages.js
echo "const svgImages = {" >> svgImages.js

while read -r line; do
  abbr=${line%.*}
  image=$abbr
  
  if [[ $abbr =~ $pattern ]];
  then 
    abbr=x$abbr 
  fi
  
  echo "  \"$image\": $abbr," >> svgImages.js
done < lines.txt

echo "}" >> svgImages.js

echo "" >> svgImages.js
echo "export default svgImages" >> svgImages.js

rm lines.txt
