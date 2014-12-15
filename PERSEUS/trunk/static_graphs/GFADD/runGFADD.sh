files=( degreeCount_forG degreePagerank_forG degreeRadius_forG radiusCount_forG ev1ev2_forG ev2ev3_forG )
grids=( 0 8 16 32 )
for i in "${files[@]}"
do
		for j in "${grids[@]}"
		do
			bash GFADD.bash $i $j
		done
done