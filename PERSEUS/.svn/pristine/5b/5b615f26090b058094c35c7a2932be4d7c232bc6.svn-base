#!/bin/bash

# Inputs:
# 1) input file with $features columns

infile=$1
features=5 #`head -n 1 $1 | awk 'BEGIN{FS="\t"};{print NF}'`
echo $features
echo $infile

# features
# nodeID
# 1 indegree, 
# 2 outdegree, 
# 3 inoutdegree
# 4 indegree_weighted
# 5 outdegree_weighted,
# 6 inoutdegree_weighted
# 7 pr_undirected, 
# 8 prblk_undirected, 
# 9 pr_directed, 
# 10 prblk_directed, 
# 11 max_radius, 
# 12 eff_radius, 
# 13 max_radiusblk, 
# 14 eff_radiusblk, 
# 15 max_radius_directed, 
# 16 eff_radius_directed, 
# 17 max_radiusblk_directed, 
# 18 eff_radiusblk_directed


feat=(nodeID indegree outdegree inoutdegree indeg-weighted outdeg-weighted inoutdeg-weighted pr-undirecrted prblk-undirected pr-directed prblk-directed max-radius eff-radius max-radiusblk eff-radiusblk max-radius-directed eff-radius-directed max-radiusblk-directed eff-radiusblk-directed)

processed=only_attr.txt
tail -n +1 $infile > $processed

for (( i=1; i<$features; i++ )); do
   for (( j=$i; j<$features; j++ )); do
    if [ "$i" != "$j" ]; then
        echo "(i,j) = ($i,$j)"
        col1=$(($i+1))
        col2=$(($j+1))
        echo "(col1, col2) = ($col1 $col2)"
        cut -f$col1,$col2 $processed | sort -n | uniq -c > tmp
        #tail -n +3 tmp > tmp2.txt

        outfile=distribution_${feat[$i]}_VS_${feat[$j]}-plot
        echo $outlfile

gnuplot << EOF

set key top right box
set logscale xy 10
set format x "10^{%L}"
set format y "10^{%L}"
set log cb 10
set format cb "10^{%L}"

set terminal postscript enhanced eps 30 color
#set datafile separator "\t"

# to create matlab-like palette
set palette defined ( 0 '#000090',\
                      1 '#000fff',\
                      2 '#0090ff',\
                      3 '#0fffee',\
                      4 '#90ff70',\
                      5 '#ffee00',\
                      6 '#ff7000',\
                      7 '#ee0000',\
                      8 '#7f0000')

set view map
set pm3d map
set nokey


set terminal postscript enhanced eps 30 color
#set datafile separator "\t"

########################################################
#############    PROPERTIES OF PLOT    #################
########################################################

set xlabel 'log(${feat[$i]})'
set ylabel 'log(${feat[$j]})'
set title ' ${feat[$i]} VS ${feat[$j]}'

### end: edit this area
#########################################################

set output "$outfile.eps"
#splot "$1" using $col1:$col2:3 with points notitle palette pt 7 ps 1
splot "tmp" using 2:3:1 with points notitle palette pt 7 ps 1
#plot "$infile" using $col1:$col2:3 with points notitle

EOF

convert -flatten -density 300 $outfile.eps $outfile.png

fi
done
done
