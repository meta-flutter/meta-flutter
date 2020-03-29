
from gclient_entries import entries

platform = 'linux-amd64'

f = open("deps.inc", "w")

f.write('SRC_URI += " \\\n')

git_index = 0

index = 0
for key, value in entries.iteritems() :

    ver = value.split('@')
    if ver[0] != 'git' :
        url = ver[0].split(':')
        line = '    git:' + url[1] + ';protocol=https;rev=' + ver[1] + ';destsuffix=' + key + ' \\\n'
        f.write(line)
    
    else :

        print(key + ' : ' +  value)
        git_index = index        

    index = index + 1

f.write('    "\n\n')
