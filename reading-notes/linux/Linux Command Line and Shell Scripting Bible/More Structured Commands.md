[Back](index.md)

# Reading Notes ~ More Structured Commands

## Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 9: More Structured Commands.md
- Pages: {259, }
- Reading Time: 31/03/2017 07:47 ~ 31/03/2017 23:00

## Content Navigation <a id="≡"></a>
- [The for Command](#TFC)
    + [Reading values in a list](#TFC-RVIL)
    + [Reading complex values in a list](#TFC-RCVIL)
    + [Reading a list from a variable](#TFC-RVFV)
    + [Reading values from a command](#TFC-RVFC)
    + [Changing the field separator](#TFC-CTFS)
    + [Reading a directory using wildcards](#TFC-RDUW)
- [The C-Style `for` Command](#TCSFC)
- [The `while` Command](#TWC)
    + [Basic while format](#TWC-BWF)
    + [Using multiple test commands](#TWC-UMTC)
- [The until Command](#TUC)
- [Nesting Loops](#NL)
- [Looping on File Data](#LOFD)
- [Controlling the Loop](#CTL)
    + [The break command](#CTL-TBC)
    + [The continue command](#CTL-TCC)
- [Processing the Output of a Loop](#PTOOL)

## Reading Notes

### The for Command <a id="TFC">[≡](#≡)</a>

The bash shell provides the for command to allow you to create a loop that iterates through a series of values. Each iteration performs a defined set of commands using one of the values in the series. The basic format of the bash shell for command is:   
```
for var in list
do
    commands
done
```
or      
```
for var in list; do
    commands
done
```
there are several different ways to specify the values in the list. The following
sections show the various ways to do that.

#### Reading values in a list <a id="TFC-RVIL">[≡](#≡)</a>

The most basic use of the for command is to iterate through a list of values defined within the for command itself:     
```
$ cat for.sh
#!/bin/bash
# basic usage of 'for' command
for word in China America
do
   echo "The next iteration station is $word"
done

$ ./for.sh
The next iteration station is China
The next iteration station is America
```

#### Reading complex values in a list <a id="TFC-RCVIL">[≡](#≡)</a>

Here’s a classic example of what can cause shell script programmers problems:   
```
$ cat for.sh
#!/bin/bash
for word in I don't know if this'll work
do
   echo word:$word
done

$ ./for.sh
word:I
word:dont know if thisll
word:work
```

The shell saw the single quotation marks within the list values and attempted to use them to define a single data value, and it really messed things up in the process.

There are two ways to solve this problem:

1. Use the escape character (the backslash) to escape the single quotation mark     
```
#!/bin/bash
#!/bin/bash
for word in I don\'t know if this\'ll work
do
   echo word:$word
done

$ ./for.sh
word:I
word:don't
word:know
word:if
word:this'll
word:work
```

2. Use double quotation marks to define the values that use single quotation marks.    
```
$ cat for.sh
#!/bin/bash
for word in I "don't" know if "this'll" "work fine"
do
   echo word:$word
done

$ ./for.sh
word:I
word:don't
word:know
word:if
word:this'll
word:work fine
```
The for command separates each value in the list with a **_space_**. If there are spaces in  the individual data values, you must accommodate them using double quotation marks:

#### Reading a list from a variable <a id="TFC-RVFV">[≡](#≡)</a>

The `for` command can iterate through the list which stored in a variable:     
```
$ cat for.sh
#!/bin/bash
countries="China America India"
countries=$countries" Japan"
for country in $countries
do
   echo "Country: $country"
done

$ ./for.sh
Country: China
Country: America
Country: India
Country: Japan
```


#### Reading values from a command <a id="TFC-RVFC">[≡](#≡)</a>

Another way to generate values for use in the list is to use the output of a command.  You use the backtick characters to execute any command that produces output, then use the output of the command in the for command:    
```
#!/bin/bash
$ cat states.data 
China Japan
America
India

$ cat for.sh
#!/bin/bash
states=`cat ./states.data`
for state in $states
do
   echo "country: $state"
done

$ ./for.sh
country: China
country: Japan
country: America
country: India
```

You’ll notice that the states file includes each state on a separate line except "China Japan", The for command iterates through the output of the cat command one line at a time if there is no spaces between words in the line. If you list a state with a space in it, the for command will still take each word as a separate value.


#### Changing the field separator <a id="TFC-CTFS">[≡](#≡)</a>

There is a special environment variable _IFS_, which is called the internal field separator.    

The IFS environment variable defines a list of characters the bash shell uses as field
separators. By default, the bash shell considers the following characters as field separators:  

1. A space
2. A tab
3. A newline

If the bash shell sees any of these characters in the data, it’ll assume that you’re starting a new data field in the list.     

But you can temporarily change the IFS environment variable values in your shell script to restrict the characters the bash shell recognizes as field separators, For example, if you want to change the IFS value to only recognize the newline character, you need to do this:    
`IFS=$’\n’`

Adding this statement to your script tells the bash shell to ignore spaces and tabs in data values. For example:    
```
#!/bin/bash
$ cat states.data 
China Japan
America
India

$ cat for.sh
#!/bin/bash
IFS=$'\n'
states=`cat ./states.data`
for state in $states
do
   echo "country: $state"
done

$ ./for.sh
country: China Japan
country: America
country: India
```
The first line "China Japan" is not seperated into two fields.

If you want to specify more than one IFS character, just string them together on the assignment line:     
```
IFS=$'\n': ;
IFS=$'\n'':'" "";"
```
The code above set IFS cahracters to:new line(\n), colon(:), space( ), semicolon(;). 
**_Note_** You can wrap the seperator character with single quotes(') or double quotes("), or wrap nothing is also ok. The effect of code above in two lines are the same.

**_Notice_** The safe practice to change the IFS is to save the original IFS value before changing it, after finished, restore it when you’re done like:    
```
IFS.OLD=$IFS
IFS=$'\n'
#use new IFS value in code
IFS=$IFS.OLD
```


#### Reading a directory using wildcards <a id="TFC-RDUW">[≡](#≡)</a>

You can use the `for` command to automatically iterate through a directory of files. To
do this, you must use a wildcard character in the file or pathname. This forces the shell to use file globbing. File globbing is the process of producing file or path names that match a specified wildcard character. For example:       
```
#!/bin/bash

$ cat for.sh 
#!/bin/bash
basedir=$HOME/shellex
for file in $HOME/shellex/*
do
   if [ -d "$file" ]
   then
      echo "$file is a directory."
   elif [ -f "$file" ]
   then 
      echo "$file is a file."
   else
      echo "$file unknow type."
   fi
done

$ ./for.sh
/home/setamv/shellex/du is a directory.
/home/setamv/shellex/scripts is a directory.
/home/setamv/shellex/test.sh is a file.
```

**_Notice_** it’s legal that directory and filenames can contain spaces. To accommodate that, you should enclose the `$file` variable in double quotation marks when used in `test` command. If you don’t, you’ll get an error if you run into a directory or filename that contains spaces.

You can use the `for` command to iterate multiple list at a time, for example:    
```
#!/bin/bash
for file in $HOME/shellex/bin/* $HOME/shellex/scripts/* `cat states.data`
do
   <process command here>
done

```

The script above first use file globbing to iterate through the list of files and directories in folder "$HOME/shellex/bin/", and then the list of files and directories in folder "$HOME/shellex/scripts", in the end, it iterate through the content list from file "states.data".


### The C-Style `for` Command <a id="TCSFC">[≡](#≡)</a>

The bash shell supports a version of the `for` loop that looks similar to the C-style `for` loop, Here’s the basic format of the C-style bash for loop:     
```
for (( variable assignment ; condition ; iteration process ))
```

Here is a C-style `for` command example:    
```
#!/bin/bash

$ cat for.sh 
#!/bin/bash
for ((i = 1; i < 4; i++))
do
   echo "The next number is $i"
done

$ ./for.sh
The next number is 1
The next number is 2
The next number is 3
```

Notice that there are a couple of things that don’t follow the standard bash shell for  method:   

1. The assignment of the variable value can contain spaces.
2. The variable in the condition isn’t preceded with a dollar sign.
3. The equation for the iteration process doesn’t use the expr command format.


The C-style `for` command also allows you to use multiple variables for the iteration. The loop handles each variable separately, for example:    
```
#!/bin/bash

$ cat for.sh 
#!/bin/bash
for ((i = 1, j = 3; i <= 3 && j >= 0; i++, j--))
do
   echo "Next i=$i, j=$j"
done

$ ./for.sh
Next i=1, j=3
Next i=2, j=2
Next i=3, j=1
```


### The `while` Command <a id="TWC">[≡](#≡)</a>

The while command allows you to define a command to test, then loop through a set
of commands for as long as the defined test command returns a zero exit status. It tests the test command at the start of each iteration. When the test command returns a non-zero exit status, the while command stops executing the set of commands.

#### Basic while format <a id="TWC-BWF">[≡](#≡)</a>

The format of the while command is:   
```
while test command
do
    other commands
done
```

The _test command_ can be any condition test commands, like `test` command, single bracket expression `[ command ]`, double parentheses expression `(( expression ))`, double bracket expression `[[ expression ]]`.

For example:    
```
#!/bin/bash
$ cat while.sh
#!/bin/bash
var1=1
while [ $var1 -lt 3 ]
do
   echo "Next var1 is $var1"
   var1=$[ $var1 + 1 ]
done

$ ./while.sh
Next var2 is 1
Next var2 is 2
```

It's the same as:     
```
#!/bin/bash
var1=1
while ((var1 < 3)) 
do
   echo "Next var1 is $var1"
   ((var1 = var1 + 1))
done
```
or    
```
#!/bin/bash
var1=1
while [[ "$var1" == "3")) 
do
   echo "Next var1 is $var1"
   ((var1 = var1 + 1))
done
```


#### Using multiple test commands <a id="TWC-UMTC">[≡](#≡)</a>

the while command allows you to define multiple test commands on the while statement line. Only the exit status of the **last** test command is used to determine when the loop stops, for example:   
```
#!/bin/bash

$ cat while.sh 
#!/bin/bash
var1=1
while echo "Start a new loop, var1=$var1"
      ((var1++))       
      [ $var1 -lt 3 ]
do
   echo "Inside loop, var1=$var1"
done

$ ./while.sh
Start a new loop, var1=1
Inside loop, var1=2
Start a new loop, var1=2
```

**_Notice_** You **MUST** put each test command on a single line, like example above, there are three test commands which occupied three lines.


### The until Command <a id="TUC">[≡](#≡)</a>

The `until` command works exactly the opposite way from the `while` command. The `until`
command requires that you to specify a test command that normally produces a non-zero exit status. As long as the exit status of the test command is non-zero, the bash shell executes the commands listed in the loop. Once the test command returns a zero exit status, the loop stops.

The format of the until command is:     
```
until test commands
do
    other commands
done
```

Example:    
```
#!/bin/bash

$ cat until.sh 
#!/bin/bash
var1=1
until echo "Start a new loop, var1=$var1"
      ((var1 = var1 + 1))
      (( var1 >= 3 ))
do
   echo "Inside loop, var1=$var1"
done

$ ./until.sh
Start a new loop, var1=1
Inside loop, var1=2
Start a new loop, var1=2
```

Other usages please refer to [The `while` Command](#TWC).


### Nesting Loops <a id="NL">[≡](#≡)</a>

A loop statement can use any other type of command within the loop, including other loop commands. This is called a nested loop. For example:     
```
#!/bin/bash

$ cat nestedloop.sh 
#!/bin/bash
for ((a = 1; a <= 3; a++))
do
   for ((b = 1; b <= 2; b++))
   do
      echo "a=$a, b=$b"
   done
done

$ ./nestedloop.sh
a=1, b=1
a=1, b=2
a=2, b=1
a=2, b=2
a=3, b=1
a=3, b=2
```


### Looping on File Data <a id="LOFD">[≡](#≡)</a>

Often, you must iterate through items stored inside a file. This requires combining two of the techniques covered:    

1. Using nested loops
2. Changing the IFS environment variable

The classic example of this is processing data in the /etc/passwd file. This requires that you iterate through the /etc/passwd file line by line, then change the IFS variable value to a colon so that you can separate out the individual components in each line, for example, the following script just output the user name and user's home directory from the /etc/passwd file:     
```
#!/bin/bash

$ cat looppasswd.sh
#!/bin/bash
IFS_OLD=$IFS
IFS=$'\n'
newline=""
for line in `tail -n 3 /etc/passwd`
do
   IFS=':'
   newline=''
   i=0
   for field in $line
   do
     ((i++))
     if ((i==1)) 
     then 
        newline="username=$field"
     elif ((i==6))
     then
        newline="$newline, home=$field"
     fi
   done
   echo $newline
   IFS=$'\n'
done
IFS=$IFS_OLD

$ ./looppasswd.sh
username=setamv, home=/home/setamv
username=susie, home=/home/susie
username=hong, home=/home/hong-home
```


### Controlling the Loop <a id="CTL">[≡](#≡)</a>

There are a couple of commands that help us control what happens inside of a loop:    

1. The `break` command
2. The `continue` command

#### The break command <a id="CTL-TBC">[≡](#≡)</a>

The break command is a simple way to escape out of a loop in progress. You can use the break command to exit out of any type of loop, including while and until loops.

##### Breaking out of a single loop

When the shell executes a break command, it attempts to break out of the loop that’s currently processing, for example:     
```
#!/bin/bash

$ cat break.sh
#!/bin/bash
for a in 1 2 3 4 5 6
do
   if [ $a -eq 3 ]
   then
      break
   fi
   echo "Iteration number $a"
done
echo "Complete iteration"

$ ./break.sh
Iteration number 1
Iteration number 2
Complete iteration
```

##### Breaking out of an outer loop

There may be times when you’re in an inner loop but need to stop the outer loop. The break command includes a single command line parameter value:    
`break n`     
where n indicates the level of the loop to break out of. By default, n is one, indicating to break out of the current loop. If you set n to a value of two, the break command will stop the next level of the outer loop, for example:    
```
#!/bin/bash

$ cat break.sh
#!/bin/bash
for a in 1 2 3 4 5 6
do
   for b in 7 8 
   do
      echo "Iteration a=$a, b=$b"
      if [ $a -eq 3 ] && [ $b -eq 8 ]
      then
         echo "Time to leave..."
         break 2
      fi
   done
done
echo "Complete iteration"

$ ./break.sh
Iteration a=1, b=7
Iteration a=1, b=8
Iteration a=2, b=7
Iteration a=2, b=8
Iteration a=3, b=7
Iteration a=3, b=8
Time to leave...
Complete iteration
```

#### The continue command <a id="CTL-TCC">[≡](#≡)</a>

The continue command is a way to prematurely stop processing commands inside of a loop
but not terminate the loop completely. This allows you to set conditions within a loop where the shell won’t execute commands. For example:     
```
#!/bin/bash

$ cat continue.sh
#!/bin/bash
for ((i = 1; i <= 5; i++))
do
   if ((i >=2 && i<=4))
   then
      continue
   fi
   echo "Iteration number $i"
done

$ ./continue.sh
Iteration number 1
Iteration number 5
```

Just as with the break command, the continue command allows you to specify what level of
loop to continue with a command line parameter:     
`continue n`    
where n defines the loop level to continue.


### Processing the Output of a Loop <a id="PTOOL">[≡](#≡)</a>

you can either pipe or redirect the output of a loop within your shell script. You do this by adding the processing command to the end of the done command:     
```
#!/bin/bash
for file in /home/*
do
    if [ -d $file ]
    then
        echo "$file is a directory."
    elif [ -f $file ]
    then
        echo "$file is a file."
    fi
done > output.txt
```

Instead of displaying the results on the monitor, the shell redirects the results of the for command to the file output.txt.

This same technique also works for piping the output of a loop to another command:  
```
#!/bin/bash
for file in /home/*
do
    if [ -d $file ]
    then
        echo "$file is a directory."
    elif [ -f $file ]
    then
        echo "$file is a file."
    fi
done | sort -r
```