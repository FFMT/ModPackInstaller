<?php
function ScanDirectory($Directory, $tableau=false){
$slash = '';
	$MyDirectory = opendir($Directory) or die('Erreur');
	while($Entry = @readdir($MyDirectory)){
		if($Entry != '.' && $Entry != '..' && $Entry != 'index.php' && $Entry != ".htaccess" && $Entry != "indexold.php" && $Entry != "test.php" && $Entry != "libraries"){
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
 
Header('Content-type: text/xml');
$xml = new SimpleXMLElement('<xml/>');
 
$base = $xml->addChild('ListBucketResult');
$base->addChild('Name', "ressources");
$base->addChild('Prefix');
$base->addChild('Marker');
$base->addChild('MaxKeys', "1000");
$base->addChild('IsTruncated', 'false');
 
foreach(ScanDirectory('.') as $key => $value){
	$stat = stat($value);
	$content = $base->addChild('Contents');
	$content->addChild('Key', htmlentities($value));
	$content->addChild('LastModified', date("Y-m-d\TH:i:s.000\Z", filemtime($value)));
	
	if(is_dir($value)&& $value != '.' && $value != '..'){
		$content->addChild('MD5', md5($value));
		$content->addChild('Size', 0);
	}else{
		$content->addChild('Size', $stat['size']);
		$content->addChild('MD5', md5_file($value));
	}
	$content->addChild('StorageClass', 'STANDARD');
}
 
print($xml->asXML());
?>