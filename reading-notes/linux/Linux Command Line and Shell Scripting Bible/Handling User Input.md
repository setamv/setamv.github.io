[Back](index.md)

# Reading Notes ~ Handling User Input

## Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 9: Handling User Input
- Pages: {259, }
- Reading Time: 31/03/2017 19:8 ~ 

## Content Navigation <a id="≡"></a>
- [Command Line Parameters](#CLP)
    + [Reading parameters](#CLP-RP)
    + [Testing parameters](#CLP-TP)
- [Special Parameter Variables](#SPV)
    + [Counting parameters](#SPV-CP)
    + [Grabbing all the data](#SPV-GATD)
- [Being Shifty](#BS)
- [Working With Options](#WWO)
    + [Finding your options](#WWO-FYO)
        * [Processing simple options](#WWO-FYO-PSO)
        * [Separating options from parameters](#WWO-FYO-SOFP)
        * [Processing options with values](#WWO-FYO-POWV)
    + [Using the getopt command](#WWO-UTGC)
        * [The command format](#WWO-UTGC-TCF)
        * [Using `getopt` in your scripts](#WWO-UTGC-UGIYS)
    + [The more advanced getopts](#WWO-TMAG)
      * [`OPTARG` environment variable](#WWO-TMAG-OAEV)
      * [`OPTIND` environment variabl](#WWO-TMAG-OIEV)
- [Standardizing Options](#SO)
- [Getting User Input](#GUI)
  + [Basic reading](#GUI-BR)
  + [Timing out](#GUI-TO)


## Reading Notes

### Command Line Parameters <a id="CLP">[≡](#≡)</a>

#### Reading parameters <a id="CLP-RP">[≡](#≡)</a>

The bash shell assigns special variables, called positional parameters, to all of the parameters entered in a command line. This even includes the name of the program the shell executes. The positional parameter variables are standard numbers, with $0 being the name of the program, $1 being the first parameter, $2 being the second parameter, and so on, up to $9 for the ninth parameter. For example:   
```
#!/bin/bash

$ cat factorial.sh 
#!/bin/bash
# To calculate the factorial of the first command line parameter.
factorial=1
for ((num=1; num<=$1; num++))
do
   factorial=$[$factorial * $num]
done
echo "\$0=$0, \$1=$1"
echo "the factorial of $1 is $factorial"

$ ./factorial.sh 4
$0=./factorial.sh, $1=4
the factorial of 4 is 24
```

**_Notice_**:

1. Each parameters is separated by a space, so if you want to include a space in the parameter, you must use quotation marks (either single or double quotation marks):     

  ```
  $ test.sh 'Rich Rum' "Rose Well"
  ```

Notice that the quotation marks aren’t part of the data, they just delineate the beginning and end of the data.

2. If your script needs more than nine command line parameters, you must use braces around the variable number after the ninth variable, such as ${10}, for example:      
     
  ```
  #!/bin/bash

  $ cat lotparams.sh 
  #!/bin/bash
  #handle more than 9 parameters 
  sum=$[$1 + $2 + $3 + $4 + $5 + $6 + $7 + $8 + $9 + $10 + ${11}]
  echo "the sum is $sum, \$10=$10, \$11=${11}"

  $ ./lotparams.sh 1 2 3 4 5 6 7 8 9 10 11
  the sum is 66, $10=10, $11=11
  ```

经过测试，不使用中括号也可以引用第9个以后的参数。


#### Testing parameters <a id="CLP-TP">[≡](#≡)</a>

When the script assumes there’s data in a parameter variable, and there isn’t, most likely you’ll get an error message from your script. This is not a good way to write scripts. It’s always a good idea to check your parameters to make sure there’s really data there before using them, like example bellow:        
```
#!/bin/bash

$ cat testparams.sh 
#!/bin/bash
#test the parameters to be not null
if [ -z "$1" ]
then
   echo "the first parameter must not be null."
else
   echo "the paramter is ok."
fi

$ ./testparams.sh
the first parameter must not be null.
```



### Special Parameter Variables <a id="SPV">[≡](#≡)</a>

There are a few special variables available in the bash shell that track command line parameters.

#### Counting parameters <a id="SPV-CP">[≡](#≡)</a>

The special `$#` variable contains the number of command line parameters included(not include the name of the program) when the script was run. For example:     
```
#!/bin/bash

$ cat countparams.sh 
#!/bin/bash
#test the count of the params
if [ $# -lt 2 ]
then
   echo "you put $# parameters which is less than 2."
fi

$ ./countparams.sh 1
you put 1 parameters which is less than 2.
```

The `$#` variable also provides a cool way of grabbing the last parameter on the command line: `${!#}`. Be careful that, use "exclamation mark" instead of "dollar sign" in bracket.    


#### Grabbing all the data <a id="SPV-GATD">[≡](#≡)</a>

The `$*` and `$@` variables provide one-stop shopping for all of your parameters. Both of these variables include all of the command line parameters within a single variable.     

The `$*` variable takes all of the parameters supplied on the command line as a single word. The word contains each of the values as they appear on the command line. Basically, instead of treating the parameters as multiple objects, the `$*` variable treats them all as one parameter.      

The `$@` variable on the other hand, takes all of the parameters supplied on the command line as separate words in the same string. It allows you to iterate through the value, separating out each parameter supplied. This is most often accomplished using the for command. For example:     
```
#!/bin/bash

$ cat iterateparams.sh 
#!/bin/bash
#use $* and $@ iterate with the parameters.
echo "\$* is: $*"
echo "\$@ is: $@"

echo ""

echo "iterate parameters by \$*:"
count=1
for param in "$*"
do
   echo "  The $count paramter in \$*: $param"
   count=$[$count + 1]
done

echo ""

echo "iterate parameters by \$@:"
count=1
for param in "$@"
do
   echo "  the $count paramter in \$@: $param"
   count=$[$count + 1]
done

$ ./iterateparams.sh setamv susie angel anhong
$* is: setamv susie angel anhong
$@ is: setamv susie angel anhong

iterate parameters by $*:
  The 1 paramter in $*: setamv susie angel anhong

iterate parameters by $@:
  the 1 paramter in $@: setamv
  the 2 paramter in $@: susie
  the 3 paramter in $@: angel
  the 4 paramter in $@: anhong
```

**_Notice_**

1. On the surface, both variables produce the same output, showing all of the command line parameters provided at once.
2. By using the for command to iterate through the special variables, The `$*` variable treated all of the parameters as a single word, while the `$@` variable treated each parameter separately.


### Being Shifty <a id="BS">[≡](#≡)</a>

The bash shell provides the `shift` command to help us manipulate command line parameters. The shift command does what it says, it **shifts** the command line parameters in their relative positions.      

When you use the `shift` command, it ‘‘downgrades’’ each parameter variable one position by default. Thus, the value for variable $3 is moved to $2, the value for variable $2 is moved to $1, and the value for variable $1 is discarded (note that the value for variable $0, the program name, remains unchanged).       

This is another great way to iterate through command line parameters, for example:  
```
#!/bin/bash

$ cat shift.sh 
#!/bin/bash
count=1
while [ -n "$1" ]
do
   echo "the $count paramter is: $1"
   shift
   ((count = count + 1))
done

$ ./shift.sh setamv susie angel anhong
the 1 paramter is: setamv
the 2 paramter is: susie
the 3 paramter is: angel
the 4 paramter is: anhong
```

Alternatively, you can perform a multiple location shift by providing a parameter to the shift command. Just provide the number of places you want to shift:    
```
#!/bin/bash

$ cat shift.sh 
#!/bin/bash
echo "all parameters are: $*"
shift 2
echo "all parameters after 'shift 2' are: $*"

$ ./shift.sh setamv susie angel anhong
all parameters are: setamv susie angel anhong
all parameters after 'shift 2' are: angel anhong
```

By using values in the shift command, you can easily skip over parameters you don’t need.   

### Working With Options <a id="WWO">[≡](#≡)</a>

#### Finding your options <a id="WWO-FYO">[≡](#≡)</a>

On the surface, there’s nothing all that special about command line options. They appear on the command line immediately after the script name, just the same as command line parameters. In fact, if you want, you can process command line options the same way that you process command line parameters.     

##### Processing simple options <a id="WWO-FYO-PSO">[≡](#≡)</a>

As you extract each individual parameter, use the case statement to determine when a parameter is formatted as an option, for example:       
```
$!/bin/bash

$ cat option.sh 
#!/bin/bash
#extracting command line options
count=1
while [ -n "$1" ]
do
   case "$1" in
      -a) echo "Found '-a' option in the $count parameter[$1]";;
      -b) echo "Found '-b' option in the $count parameter[$1]";;
      -c) echo "Found '-c' option in the $count parameter[$1]";;
      *) echo "Not found option in the $count parameter[$1]";;
   esac
   count=$[count + 1]
   shift
done

$ ./option.sh -a val -b -c -d
Found '-a' option in the 1 parameter[-a]
Not found option in the 2 parameter[val]
Found '-b' option in the 3 parameter[-b]
Found '-c' option in the 4 parameter[-c]
Not found option in the 5 parameter[-d]
```

##### Separating options from parameters <a id="WWO-FYO-SOFP">[≡](#≡)</a>

Often you’ll run into situations where you’ll want to use both options and parameters for a shell script. The standard way to do this in Linux is to separate the two with a special character code that tells the script when the options are done and when the normal parameters start.       

For Linux, this special character is the double dash (--). The shell uses the double dash to indicate the end of the option list. After seeing the double dash, your script can safely process the remaining command line parameters as parameters and not options.

For example:    
```
#!/bin/bash

$ cat option.sh 
#!/bin/bash
#extracting command line options
while [ -n "$1" ]
do
   case "$1" in
      -a) echo "Found '-a' option";;
      -b) echo "Found '-b' option";;
      -c) echo "Found '-c' option";;
      --) shift
          break;;
      *) echo "$1 is not an option";;
   esac
   shift
done

count=1
for param in "$@"
do
   echo "Parameter #$count is: $param"
   count=$[count + 1]
done

$ ./option.sh -a -b -- setamv susie
Found '-a' option
Found '-b' option
Parameter #1 is: setamv
Parameter #2 is: susie
```


##### Processing options with values <a id="WWO-FYO-POWV">[≡](#≡)</a>

Some options require an additional parameter value. In these situations, the command line looks something like this:    
```
$ ./option.sh -a test1 -b -c -d test2
```

Your script must be able to detect when your command line option requires an additional parameter and be able to process it appropriately. Here’s an example of how to do that:     
```
#!/bin/bash

$ cat option.sh
#!/bin/bash
#extracting command line options and values
while [ -n "$1" ]
do
   case "$1" in
      -a) echo "Found the -a option";;
      -b) param="$2"
          echo "Found the -b option, with parameter value $param"
          shift 2;;
      -c) echo "Found the -c option";;
      --) shift
          break;;
      *) echo "$1 is not an option";;
   esac
   shift
done
count=1
for param in "$@"
do
   echo "Parameter #$count: $param"
   count=$[ $count + 1 ]
done

$ ./option.sh -a -b test1 -d
Found the -a option
Found the -b option, with parameter value test1
-d is not an option
```

#### Using the getopt command <a id="WWO-UTGC">[≡](#≡)</a>

The getopt command is a great tool to have handy when processing command line options and
parameters. It reorganizes the command line parameters to make parsing them in your script easier.      

###### The command format <a id="WWO-UTGC-TCF">[≡](#≡)</a>

The getopt command can take a list of command line options and parameters, in any form, and automatically turn them into the proper format. It uses the command format:     
`getopt options optstring parameters`

The optstring is the key to the process. It defines the valid option letters used in the command line. It also defines which option letters require a parameter value.      

First, list each command line option letter you’re going to use in your script in the optstring. Then, place a colon after each option letter that requires a parameter value. The getopt command parses the supplied parameters based on the optstring you define. for example:        
```
$ getopt ab:cd -a -b test1 -cd test2 test3
-a -b test1 -c -d -- test2 test3
```
The optstring defines four valid option letters, a, b, c, and d. It also defines that the option letter b requires a parameter value.

##### Using getopt in your scripts <a id="WWO-UTGC-UGIYS">[≡](#≡)</a>

You can use `set` command to reset the command line parameters, because one of the options of the `set` command is the double dash(--), which instructs it to replace the command line parameter variables with the values on the set command’s command line, the format is as follows:         
```
set -- `getopt -q ab:cd "$@"`
```
The command above uses `getopt` command to format the command line parameters, and then uses `set` command to replace the command line parameter variables with the formatted forms. 

Example:       
```
$ cat option.sh 
#!/bin/bash
echo "Origin options and parameter: $@"

set -- `getopt -q ab:cd "$@"`
echo "Formatted options and parameters: $@"

$ ./option.sh -a -b test -cd
Origin options and parameter: -a -b test -cd setamv susie
Formatted options and parameters: -a -b 'test' -c -d -- 'setamv' 'susie'
```


#### The more advanced getopts <a id="WWO-TMAG">[≡](#≡)</a>

The `getopts` command (notice that it’s plural) is built into the bash shell. It looks a lot like its getopt cousin, but has some expanded features.    

Unlike getopt, which produces one output for all of the processed options and parameters found in the command line, the getopts command works on the existing shell parameter variables sequentially.       

It processes the parameters it detects in the command line one at a time each time it’s called. When it runs out of parameters, it exits with an exit status greater than zero.  

The format of the getopts command is:   
`getopts optstring variable`
The optstring value is similar to the one used in the getopt command. List valid option
letters in the optstring, along with a colon if the option letter requires a parameter value. To suppress error messages, start the optstring with a colon. The getopts command places the current parameter in the variable defined in the command line.

There are two environment variables that the `getopts` command uses:      

##### `OPTARG` environment variable <a id="WWO-TMAG-OAEV">[≡](#≡)</a>

The `OPTARG` environment variable contains the value to be used if an option requires a parameter value. For example:       
```
#!/bin/bash

$ cat option.sh 
#!/bin/bash
while getopts :ab:c opt
do
   case "$opt" in
      a) echo "Found the '-a' option.";;
      b) echo "Found the '-b' option with option value '$OPTARG'.";;
      c) echo "Found the '-c' option.";;
      *) echo "Unknown option: $opt";;
   esac
done

$ ./option.sh -ab test -cd
Found the '-a' option.
Found the '-b' option with option value 'test'.
Found the '-c' option.
Unknown option: ?
```

**_Notice_**

- When the getopts command parses the command line options, it also strips off the leading dash, so you don’t need them in the case definitions.        
- you can run the option letter and the parameter value together without a space like:      
    ```
    ./option.sh -abtest1
    Found the -a option
    Found the -b option, with value test1
    ```
- Any option letter not defined in the optstring value is sent to your code as a question mark



##### `OPTIND` environment variable <a id="WWO-TMAG-OIEV">[≡](#≡)</a>

The `OPTIND` environment variable contains the value of the current location within the parameter list where getopts left off. As `getopts` processes each option, it increments the `OPTIND` environment variable by one, When you’ve reached the end of the getopts processing, you can just use the OPTIND value with the shift command to move to the parameters, for example:      
```
#!/bin/bash

$ cat option.sh 
#!/bin/bash
while getopts :ab:c opt
do
   case "$opt" in
      a) echo "Found the '-a' option.";;
      b) echo "Found the '-b' option with option value '$OPTARG'.";;
      c) echo "Found the '-c' option.";;
      *) echo "Unknown option: $opt";;
   esac
done
shift $[$OPTIND - 1]
while [ -n "$1" ]
do
   echo "Found parameter $1"
   shift
done

$ $ ./option.sh -abtest -cd setamv susie
Found the '-a' option.
Found the '-b' option with option value 'test'.
Found the '-c' option.
Unknown option: ?
Found parameter setamv
Found parameter susie
```


### Standardizing Options <a id="SO">[≡](#≡)</a>

there are a few letter options that have achieved somewhat of a standard meaning in the Linux world. If you leverage these options in your shell script, it’ll make your scripts more user-friendly.    

| **Option |                 **Description**                  |
|----------|--------------------------------------------------|
| -a       | Show all objects                                 |
| -c       | Produce a count                                  |
| -d       | Specify a directory                              |
| -e       | Expand an object                                 |
| -f       | Specify a file to read data from                 |
| -h       | Display a help message for the command           |
| -i       | Ignore text case                                 |
| -l       | Produce a long format version of the output      |
| -n       | Use a non-interactive (batch) mode               |
| -o       | Specify an output file to redirect all output to |
| -q       | Run in quiet mode                                |
| -r       | Process directories and files recursively        |
| -s       | Run in silent mode                               |
| -v       | Produce verbose output                           |
| -x       | Exclude an object                               |
| -y       | Answer yes to all questions                      |


### Getting User Input <a id="GUI">[≡](#≡)</a>

There are times when you need to ask a question while the script is running and wait for a response from the person running your script. The bash shell provides the `read` command just for this purpose.

#### Basic reading <a id="GUI-BR">[≡](#≡)</a>

The `read` command accepts input from the standard input (the keyboard), or from another file descriptor. After receiving the input, the read command places the data into a
standard variable. For example:   
```
$ cat read.sh 
#!/bin/bash
#basic read
echo -n "please enter your name: "
read name
echo "your name is $name"

$ ./read.sh
please enter your name: setamv
your name is setamv
```

Notice that the `echo` command that produced the prompt uses the _-n_ option. This suppresses the newline character at the end of the string, allowing the script user
to enter data immediately after the string. The `read` command includes the `-p` option, which provide the same usage as `echo -n` and allows you to specify a prompt directly
in the `read` command line:   
```
$ cat read.sh 
#!/bin/bash
#basic read
read -p "please enter your name: " name
echo "your name is $name"

$ ./read.sh
please enter your name: setamv
your name is setamv
```

You can specify multiple variables. Each data value entered is assigned to the next variable in the list. If the list of variables runs out before the data does, the remaining data is assigned to the last variable, for example:   
```
$ cat read.sh 
#!/bin/bash
#basic read
read -p "Please enter your first name and last name: " firstname lastname
echo "your first name is '$firstname', last name is '$lastname'"

./read.sh 
Please enter your first name and last name: setamv luo anythingelse
your first name is 'setamv', last name is 'luo anythingelse'

./read.sh
Please enter your first name and last name: "setamv luo" anythingelse
your first name is '"setamv', last name is 'luo" anythingelse'

./read.sh
Please enter your first name and last name: setamv\ luo anythingelse
your first name is 'setamv luo', last name is 'anythingelse'
```

**_Notice_**    

1. The second variable "lastname" received the value "luo anythingelse". 
2. If you want to place a whitespace in the value, you cannot wrap the words and whitespace in double quotation or single quotation like "'setamv luo'", you have to escape the whitespace like "\ ".

You can also specify no variables on the read command line. If you do that, the read command places any data it receives in the special environment variable `REPLY`:

#### Timing out <a id="GUI-TO">[≡](#≡)</a>