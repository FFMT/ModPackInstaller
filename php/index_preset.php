<?php
function ScanDirectory($Directory, $tableau=false)
{
    $slash = '';
    $MyDirectory = opendir($Directory) or die('Erreur');
    while($Entry = @readdir($MyDirectory))
    {
        if($Entry != '.' && $Entry != '..' && $Entry != 'index.php' && $Entry != ".htaccess")
        {
            if(is_dir($Directory.'/'.$Entry)&& $Entry != '.' && $Entry != '..')
            {
                $slash = '/';			
            }
            else
            {
                $slash = '';
            }
            $tableau[] = substr($Directory.'/'.$Entry, strlen(strstr($Directory.'/'.$Entry, '/', true))+1).$slash;
        }
        if(is_dir($Directory.'/'.$Entry)&& $Entry != '.' && $Entry != '..')
        {
            $tableau = ScanDirectory($Directory.'/'.$Entry, $tableau);
        }
    }
    closedir($MyDirectory);
    return $tableau;
}

function valid($value)
{
   return !is_dir($value) || substr_count($value, '/') == 1;
}

header('Content-type: text/javascript');
$index = 0;
echo '{'."\n";
echo '    "default": "azerty",'."\n";

$index = 0;
$lastCategory = "";
foreach(ScanDirectory('.') as $key => $value)
{
    if(!is_dir($value))
    {
        $currentCategory = substr($value, 0, strpos($value,"/"));
        if($currentCategory != $lastCategory)
        {
            $lastCategory = $currentCategory;
            if($index != 0)
            {
                echo "\n".'    ],'."\n";
            }
            echo '    "'.$currentCategory.'": ['."\n";
            $index = 0;
        }
        if($index != 0)
        {
            echo ","."\n";
        }
        echo '        '.'"'.htmlentities(substr($value, strpos($value,"/") + 1, strlen($value))).'"';
        
        $index++;
    }
}
echo "\n";
echo '    ]'."\n";
echo '}';

?>
