#!/bin/bash

###############################
# Version 0.1                 #
# Plotting 2d distributions   #
# and anomalous points        #
# ProdigalNet: September Data #
# Author: Danai Koutra        #
# Requirement: gnuplot        #
###############################

infile=$1
features=$2
echo $features
echo $distFile

# feat1/2 are:
# 1 for fileEvents
# 2 for fixedEventCnt
# 3 for remEventCnt
# 4 for netEventCnt
# 5 for cdEventCnt
# 6 for remDrivesCnt
# 7 for copiesToRem
# 8 for copiesFromRem
# 9 for fileCreationCnt
# 10 for folderCreationCnt
# 11 for distFilesCnt
# 12 for filesOnRemCnt
# 13 for distWorkstationCnt

feat=(ignore fileEvents fixedEventCnt remEventCnt netEventCnt cdEventCnt distRemDrivesCnt copiesToRem copiesFromRem fileCreationCnt folderCreationCnt distFilesCnt filesOnRemCnt distWorkstationCnt)

for (( i=1; i<=$features; i++ )); do
   for (( j=$i; j<=$features; j++ )); do
    if [ "$i" != "$j" ]; then
    	echo "(i,j) = ($i,$j)"
    	col1=$(($i+2))
	col2=$(($j+2))
    	echo "(col1, col2) = ($col1 $col2)"
    	#cut -f $col -d, $infile | sort -n | uniq -c > tmp
    	#tail -n +3 tmp > tmp2.txt

    	outfile=distribution_${feat[$i]}_VS_${feat[$j]}-plot.png
    	echo $outlfile

gnuplot << EOF

set terminal png #font "/Library/Fonts/Arial.ttf" 14
#set terminal png  #postscript eps enhanced
 set output "$outfile"
 set nokey
 set datafile separator ","
 set logscale
 set xlabel 'log(${feat[$i]})'
 set ylabel 'log(${feat[$j]})'
 set title ' ${feat[$i]} VS ${feat[$j]}'
# set xtics 1
# set format x '10^(%.0f)'
 plot '$infile' using $col1:$col2 lt 3 with dots, \
     './fileFeatures_anomaly0_IncBy1.txt' using $col1:$col2 lt 2, \
     './fileFeatures_anomaly1_IncBy1.txt' using $col1:$col2 lt 1, \
     './fileFeatures_anomaly2_IncBy1.txt' using $col1:$col2 lt -1, \
     './fileFeatures_anomaly3_IncBy1.txt' using $col1:$col2 lt 6 pt 6, \
     './fileFeatures_anomaly4_IncBy1.txt' using $col1:$col2 lt 4, \
     './fileFeatures_anomaly5_IncBy1.txt' using $col1:$col2 lt 5, \
     './fileFeatures_anomaly6_IncBy1.txt' using $col1:$col2 lt 6, \
     './fileFeatures_anomaly7_IncBy1.txt' using $col1:$col2 lt 1 pt 5, \
     './fileFeatures_anomaly8_IncBy1.txt' using $col1:$col2 lt 8, \
     './fileFeatures_anomaly9_IncBy1.txt' using $col1:$col2 lt 1 pt 6, \
     './fileFeatures_anomaly10_IncBy1.txt' using $col1:$col2 lt 2 pt 6, \
     './fileFeatures_anomaly11_IncBy1.txt' using $col1:$col2 lt 4 pt 6, \
     './fileFeatures_anomaly12_IncBy1.txt' using $col1:$col2 lt 5 pt 6

EOF

fi
done
done   



#plot './temp1' using (log($1)):(log($2)) lt -1, \
#     './temp2' u (log($1)):(log($2)):(0.5*\$3) w points lt 1 pt 8 ps variable


#mv $outfile lof/
#echo "\n$outfile saved in lof directory.\n"

