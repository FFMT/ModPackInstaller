<?php
function ScanDirectory($Directory, $tableau=false){
    $slash = '';
	$MyDirectory = opendir($Directory) or die('Erreur');
	while($Entry = @readdir($MyDirectory)){
		if($Entry != '.' && $Entry != '..' && $Entry != 'index.php' && $Entry != ".htaccess"){
			if(is_dir($Directory.'/'.$Entry)&& $Entry != '.' && $Entry != '..'){
				$slash = '/';			
			}
            else
            {
                $slash = '';
            }
			$tableau[] = substr($Directory.'/'.$Entry, strlen(strstr($Directory.'/'.$Entry, '/', true))+1).$slash;
		}
		if(is_dir($Directory.'/'.$Entry)&& $Entry != '.' && $Entry != '..'){
			$tableau = ScanDirectory($Directory.'/'.$Entry, $tableau);
		}
	}
	closedir($MyDirectory);
	return $tableau;
}

header('Content-type: text/javascript');
echo '['."\n";
$index = 0;
foreach(ScanDirectory('.') as $key => $value)
{
	$stat = stat($value);
	if($index != 0)
	{
		echo ", "."\n";
	}
	echo '    {'."\n";
	echo '        "name":"'.htmlentities($value).'",'."\n";
	if(is_dir($value)){
	        echo '        "md5":"'.md5($value).'",'."\n";
	        echo '        "size":"0"'."\n";
	}else{
	        echo '        "md5":"'.md5_file($value).'",'."\n";
	        echo '        "size":"'.$stat['size'].'"'."\n";
	}
	echo '    }';
	$index++;
}
echo "\n".']';

?>
