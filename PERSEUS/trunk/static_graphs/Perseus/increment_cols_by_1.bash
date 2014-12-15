#!/bin/bash

awk -F"," 'BEGIN { FS = ","; OFS=","} ; { $2++; $3++; $4++; $5++; $6++; $7++; $8++; $9++; $10++; $11++; $12++; $13++; $14++; $15++; $16++; $17++; $18++; $19++; print $0 }' .txt > filesFeaturesPerUser_September_temporal_IncBy1.txt
