#!/bin/bash

##############################
# Version 0.1                #
# Plotting 2d-points         #
# Author: Danai Koutra       #
# Modified by Jay-Yoon 	     #
# Requirement: gnuplot       #
##############################

infile=$1
outlier_points=$2
outfile=$1-$5_g-plot.png
col1=$3
col2=$4
echo "3:${3},4:${4}"
gnuplot << EOF

set terminal png ##postscript eps enhanced
set logscale xy 10
set format x "10^{%L}"
set format y "10^{%L}"
set title "${6} vs ${7}"
set xlabel "log(${6})"
set ylabel "log(${7})"

set output "$outfile"
set nokey

plot './$infile' every ::2 using $3:$4 lt -1, \
 './$outlier_points' u 3:4:(0.5*\$5):(1.0*\$6) w points lt 1 pt 8 lc variable ps variable

EOF

mv $outfile gfadd/ #lof_tw_deg_tri/
echo "\n$outfile saved in lof directory.\n"
