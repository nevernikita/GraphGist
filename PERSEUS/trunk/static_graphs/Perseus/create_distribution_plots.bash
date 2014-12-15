#!/bin/bash

# Inputs:
# 1) input file with 2 columns
# 2) title
# 3) x-axis label
# 4) y-axis label
# 5) file of output file

/usr/local/bin/gnuplot << EOF

#set key top right box
unset key
set logscale xy 10
set format x "10^{%L}"
set format y "10^{%L}"

set terminal postscript enhanced eps 30 color
set datafile separator "\t"

########################################################
#############           PLOT           #################
########################################################
set title "$2"
set xlabel "$3"
set ylabel "$4"

#set xlabel "degree"
#set ylabel "count" -1.2,0
#set title "Unweighted Polly Graph: degree distributions"

### end: edit this area
#########################################################

set output "$5.eps"
plot "$1" using (\$1+1):2 with points pt 7 ps 1 

EOF

/opt/local/bin/convert -flatten -density 300 $5.eps $5.png
