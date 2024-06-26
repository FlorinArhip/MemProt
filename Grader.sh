#**********************
#*
#* Progam Name: MP. Membership Protocol.
#*
#* Current file: Grader.sh
#* About this file: Grading Script.
#* 
#***********************
#!/bin/sh

function contains () {
  	local e
  	for e in "${@:2}"
	do 
		if [ "$e" == "$1" ]; then 
			echo 1
			return 1;
		fi
	done
  	echo 0
}

verbose=$(contains "-v" "$@")
grade=0

echo "============================================"
echo "Grading Started"
echo ""
echo "============================================"
echo "Single Failure Scenario"
echo "============================================"
ant dist
java -jar dist/MemProt.jar cases/singlefailure.conf > /dev/null
joincount=`grep joined dbg.log | cut -d" " -f2,4-7 | sort -u | wc -l`
if [ $joincount -eq 100 ]; then
	grade=`expr $grade + 10`
	echo "Checking Join..................10/10"
else
	joinfrom=`grep joined dbg.log | cut -d" " -f2 | sort -u`
	cnt=0
	for i in $joinfrom
	do
		jointo=`grep joined dbg.log | grep '^ '$i | cut -d" " -f4-7 | grep -v $i | sort -u | wc -l`
		if [ $jointo -eq 9 ]; then
			cnt=`expr $cnt + 1`
		fi
	done
	if [ $cnt -eq 10 ]; then
		grade=`expr $grade + 10`
		echo "Checking Join..................10/10"
	else
		echo "Checking Join..................0/10"
	fi
fi
failednode=`grep "Node failed at time" dbg.log | sort -u | awk '{print $1}'`
failcount=`grep removed dbg.log | sort -u | grep $failednode | wc -l`
if [ $failcount -ge 9 ]; then
	grade=`expr $grade + 10`
	echo "Checking Completeness..........10/10"
else
	echo "Checking Completeness..........0/10"
fi
failednode=`grep "Node failed at time" dbg.log | sort -u | awk '{print $1}'`
accuracycount=`grep removed dbg.log | sort -u | grep -v $failednode | wc -l`
if [ $accuracycount -eq 0 ] && [ $failcount -gt 0 ]; then
	grade=`expr $grade + 10`
	echo "Checking Accuracy..............10/10"
else
	echo "Checking Accuracy..............0/10"
fi
echo "============================================"
echo "Multi Failure Scenario"
echo "============================================"
ant dist
java -jar dist/MemProt.jar cases/multifailure.conf > /dev/null
joincount=`grep joined dbg.log | cut -d" " -f2,4-7 | sort -u | wc -l`
if [ $joincount -eq 100 ]; then
	grade=`expr $grade + 10`
	echo "Checking Join..................10/10"
else
	joinfrom=`grep joined dbg.log | cut -d" " -f2 | sort -u`
	cnt=0
	for i in $joinfrom
	do
		jointo=`grep joined dbg.log | grep '^ '$i | cut -d" " -f4-7 | grep -v $i | sort -u | wc -l`
		if [ $jointo -eq 9 ]; then
			cnt=`expr $cnt + 1`
		fi
	done
	if [ $cnt -eq 10 ]; then
		grade=`expr $grade + 10`
		echo "Checking Join..................10/10"
	else
		echo "Checking Join..................0/10"
	fi
fi
failednode=`grep "Node failed at time" dbg.log | sort -u | awk '{print $1}'`
tmp=0
cnt=0
for i in $failednode
do
	failcount=`grep removed dbg.log | sort -u | grep $i | wc -l`
	if [ $failcount -ge 5 ]; then
		tmp=`expr $tmp + 2`
		grade=`expr $grade + 2`
	fi
        cnt=`expr $cnt + 1`
        if [ $cnt -gt 5 ]; then
                break
        fi
done
echo "Checking Completeness..........$tmp/10"
failednode=`grep "Node failed at time" dbg.log | sort -u | awk '{print $1}'`
tmp=0
for i in $failednode
do
	accuracycount=`grep removed dbg.log | sort -u | grep -v $i | wc -l`
	if [ $accuracycount -ge 20 ]; then
		tmp=`expr $tmp + 2`
		grade=`expr $grade + 2`
	fi
        if [ $tmp -gt 9 ]; then
                break
        fi
done
echo "Checking Accuracy..............$tmp/10"
echo "============================================"
echo "Message Drop Single Failure Scenario"
echo "============================================"
ant dist
java -jar dist/MemProt.jar cases/msgdropsinglefailure.conf > /dev/null
joincount=`grep joined dbg.log | cut -d" " -f2,4-7 | sort -u | wc -l`
if [ $joincount -eq 100 ]; then
	grade=`expr $grade + 15`
	echo "Checking Join..................15/15"
else
	joinfrom=`grep joined dbg.log | cut -d" " -f2 | sort -u`
	cnt=0
	for i in $joinfrom
	do
		jointo=`grep joined dbg.log | grep '^ '$i | cut -d" " -f4-7 | grep -v $i | sort -u | wc -l`
		if [ $jointo -eq 9 ]; then
			cnt=`expr $cnt + 1`
		fi
	done
	if [ $cnt -eq 10 ]; then
		grade=`expr $grade + 15`
		echo "Checking Join..................15/15"
	else
		echo "Checking Join..................0/15"
	fi
fi
failednode=`grep "Node failed at time" dbg.log | sort -u | awk '{print $1}'`
failcount=`grep removed dbg.log | sort -u | grep $failednode | wc -l`
if [ $failcount -ge 9 ]; then
	grade=`expr $grade + 15`
	echo "Checking Completeness..........15/15"
else
	echo "Checking Completeness..........0/15"
fi
failednode=`grep failed dbg.log | sort -u | awk '{print $1}'`
accuracycount=`grep removed dbg.log | sort -u | grep -v $failednode | wc -l`
if [ $accuracycount -eq 0 ] && [ $failcount -gt 0 ]; then
	grade=`expr $grade + 10`
	echo "Checking Accuracy..............10/10"
else
	echo "Checking Accuracy..............0/10"
fi
echo "============================================"
echo Final grade $grade
