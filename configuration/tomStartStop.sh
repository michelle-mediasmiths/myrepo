#!/bin/sh


pgt=/storage/corp/placeholders/Progratron


if [ -d "$pgt" ] ; then

    echo "Remove the PrograTron Processing Items in " $pgt

    for f in $pgt/processing/*.xml ; do

         echo "Moving $f to $pgt";

         mv  $f $pgt

    done
else
    echo "PrograTron Folder  " $pgt " does not exist "
fi

for p in /storage/mam/hires01/drop1/technicolor /storage/mam/hires03/drop3/redbee /storage/mam/hires02/drop2/adults /storage/mam/hires04/drop4/batch/hd /storage/mam/hires04/drop4/batch/sd ; do


	echo "Processing $p"

	if [ -d "$p" ] ; then

        echo "Remove the $p Processing Items"

         for f in $p/processing/* ; do

             if [ ! -d $f ] ; then

                 echo "Moving $f to $p";

                mv  $f $p
             fi

         done
else
    echo "Folder  " $p " does not exist "
fi

done
