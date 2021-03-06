[Back](index.md)

# Creating Functions

# Introduction


# Content Catalogue <a id="≡"></a>

- [Basic Script Functions](#BSF)
    + [Creating a function](#BSF-CF)
    + [Using functions](#BSF-UF)
    + [Returning a Value](#BSF-RV)
    + [Using function output](#BSF-UFO)
- [Using Variables in Functions](#UVIF)
    + [Passing parameters to a function](#UVIF-PP)
    + [Handling variables in a function](#UVIF-HV)
        * [Global variables](#UVIF-HV-GV)
        * [Local variables](#UVIF-HV-LV)
- [Array Variables and Functions](#AVAF)
    + [Passing arrays to functions](#AVAF-PATF)
    + [Returning arrays from functions](#AVAF-RAFF)
- [Function Recursion](#FR)
- [Creating a Library](#CAL)

# Content

## Basic Script Functions <a id="BSF">[≡](#≡)</a>

### Creating a function <a id="BSF-CF">[≡](#≡)</a>

There are two formats you can use to create functions in bash shell scripts:  

1. uses the keyword `function`

    You can use the keyword `function` along with the function name you assign to the block of code:    
    ```
    function name {
        commands
    }
    ```

    The _name_ attribute defines a unique name assigned to the function. Each function you define in your script must be assigned a unique name.

2. uses empty parentheses

    The format is:  
    ```
    name() {
        commands
    }
    ```

### Using functions <a id="BSF-UF">[≡](#≡)</a>

To use a function in your script, specify the function name on a line, just as you would any other shell command:  
```
#!/bin/bash

$ cat hellofunc.sh 
#!/bin/bash
function func1 {
  echo "message from func1"
}

func2() {
  echo "message from func2"
}

func1
func2

$ ./hellofunc.sh
message from func1
message from func2
```

**Notice**: 

1. The function definition doesn’t have to be the first thing in your shell script, But if you attempt to use a function before it’s defined, you’ll get an error message
2. Each function name must be unique, if you redefine a function, the new definition will override the original function definition, without producing any error messages.


### Returning a Value <a id="BSF-RV">[≡](#≡)</a>

The bash shell treats functions like mini-scripts, complete with an exit status, There are three different ways you can generate an exit status for your functions:

1. The default exit status          
    
    By default, the exit status of a function is the exit status returned by the last command in the function. After the function executes, you can use the standard `$?`variable to determine the exit status of the function:

    ```
    #!/bin/bash

    $ cat hellofunc.sh 
    #!/bin/bash
    function func1 {
      echo "message from func1"
    }

    func2() {
      echo "message from func2"
      ls -l badfile
    }

    func1
    echo "the func1 returned status of $?"
    func2
    echo "the func2 returned status of $?"

    $ ./hellofunc.sh 
    message from func1
    the func1 returned status of 0
    message from func2
    ls: badfile: No such file or directory
    the func2 returned status of 1
    ```

2. Using the return command

    The bash shell uses the `return` command to exit a function with a specific exit status. The `return` command allows you to specify a single integer value(must be in the range of 0 to 255) to define the function exit status:    
    ```
    #!/bin/bash

    $ cat hellofunc.sh 
    #!/bin/bash
    function func {
      echo "the function will return status with 1"
      return 1
    }

    func
    echo "returned status of func is $?"

    $ ./hellofunc.sh
    the function will return status with 1
    returned status of func is 1
    ```

    **_Note_**: 如果返回一个大于255的数值，通过'$?'取到的值将是返回值对256取余。如，当返回257时，'$?'的值为：257 % 256 = 1


### Using function output <a id="BSF-UFO">[≡](#≡)</a>

Just as you can capture the output of a command to a shell variable, you can also capture the output of a function to a shell variable. You can use this technique to retrieve any type of output from a function to assign to a variable:    
```
result=`your-func`
```

Here is an example:  
```
#!/bin/bash

$ cat ./hellofunc.sh 
#!/bin/bash
function func {
  read -p "Please enter the return value: " val
  echo $[ $val * 2 ]
}

result=`func`
echo "result is $result"

$ ./hellofunc.sh
Please enter the return value: 400
result is 800
```


## Using Variables in Functions <a id="UVIF">[≡](#≡)</a>

### Passing parameters to a function <a id="UVIF-PP">[≡](#≡)</a>

You can pass parameters to a function just like a regular script, Please refere to [Handling User Input](Handling User Input.md#CLP)

Let look at an example:     
```
#!/bin/bash

$ cat hellofunc.sh 
#!/bin/bash
function addem {
  if [ $# -eq 0 ] || [ $# -gt 2 ]; then
    echo -1
  elif [ $# -eq 1 ]; then
    echo $[ $1 + $1 ]
  else
    echo $[ $1 + $2 ]
  fi
}

echo "addem 1 2 = `addem 1 2`"
echo "addem 1 = `addem 1`"
echo "addem = `addem`"

$ ./hellofunc.sh 
addem 1 2 = 3
addem 1 = 2
addem = -1
```

**_Notice_**: 
Since the function uses the same special parameter environment variables as the script parameter for its own parameter values, it can’t directly access the script parameter values from the command line of the script. This example will fail:    
```
#!/bin/bash

$ cat hellofunc.sh 
#!/bin/bash
function showparam {
  echo "$1"
}

showparam

$ ./hellofunc.sh hello

```
The function shwoparam cannot access the script parameter "hello", but you can pass the script parameters to the function:  
```
#!/bin/bash

$ cat hellofunc.sh 
#!/bin/bash
function showparam {
  echo "$1"
}

showparam $1

$ ./hellofunc.sh hell,func
hell,func
```


### Handling variables in a function <a id="UVIF-HV">[≡](#≡)</a>

Variables defined in functions can have a different scope than regular variables. There are two types of variables in function: Global and Local

#### Global variables <a id="UVIF-HV-GV">[≡](#≡)</a>

Global variables are variables that are valid anywhere within the shell script. 

1. If you define a global variable in the main section of a script, you can retrieve its value inside a function. 
2. If you define a global variable inside a function, you can retrieve its value in the main section of the script.

By default, any variables you define in the script are global variables. Variables defined outside of a function can be accessed within the function just fine:    
```
#!/bin/bash

$ cat hellofunc.sh 
#!/bin/bash
function showvalue {
  funcvar="variable in function."
  echo "$value"
}
read -p "please input a value: " value
showvalue
echo "$funcvar"

$ ./hellofunc.sh 
please input a value: 100
100
variable in function.
```
You will noticed that:

1. The variable "value" is defined outside of the function and even after the function definition. the function can use it.
2. The variable "funcvar" is defined in function, and it can be used in script which is outside of the function. But pay attention that, you must use the "funcvar" variable after the function "showvalue" is executed.

**_Notice_**: The function can use a variable which is defined after the function's definition, like the "value" variable in the example above.


#### Local variables <a id="UVIF-HV-LV">[≡](#≡)</a>

Instead of using global variables in functions, any variables that the function uses internally can be declared as local variables. To do that, just use the local keyword in front of the variable declaration:    
```
local temp
```

You can also use the local keyword in an assignment statement while assigning a value to the variable:    
```
local temp=$[ $value + 5 ]
```

The local keyword ensures that the variable is limited to only within the function. If a variable with the same name appears outside the function in the script, the shell keeps the two variable values separate（函数里面定义的本地变量会覆盖脚本中定义的同名变量，覆盖的作用域仅限于函数内部）. 
```
#!/bin/bash

$ cat hellofunc.sh
#!/bin/bash
function func1 {
    local temp=$[ $value + 5 ]
    result=$[ $temp * 2 ]
}
temp=4
value=6
func1
echo "The result is $result"
if [ $temp -gt $value ]
then
    echo "temp is larger"
else
    echo "temp is smaller"
fi
$ ./hellofunc.sh
The result is 22
temp is smaller
$
```


## Array Variables and Functions <a id="AVAF">[≡](#≡)</a>

### Passing arrays to functions <a id="AVAF-PATF">[≡](#≡)</a>

If you try using the array variable as a function parameter, the function only picks up the first value of the array variable. for example:     
```
#!/bin/bash

$ cat arrparams.sh 
#!/bin/bash
function testit {
  echo "The parameters are: $@"
  thisarray=$1
  echo "The received array is: ${thisarray[*]}"
}

myarray=(1 2 3 4 5)
echo "The original array is ${myarray[*]}"
testit $myarray

$ ./arrparams.sh 
The original array is 1 2 3 4 5
The parameters are: 1
The received array is: 1
```

To solve this problem, you must disassemble the array variable into its individual values, then use the values as function parameters. Inside the function, you can reassemble all of the parameters into a new array variable. For example:   
```
#!/bin/bash

$ cat arrparams.sh 
#!/bin/bash
function testit {
  echo "The parameters are: $@"
  thisarray=(`echo $@`)
  echo "The received array is: ${thisarray[*]}"
}

myarray=(1 2 3 4 5)
echo "The original array is ${myarray[*]}"
testit ${myarray[*]}

$ ./arrparams.sh 
The original array is 1 2 3 4 5
The parameters are: 1 2 3 4 5
The received array is: 1 2 3 4 5
```

In the example above, it's disassemble the array variable by `${myarray[*]}` and reassemble the array by ```(`echo $@`)```


### Returning arrays from functions <a id="AVAF-RAFF">[≡](#≡)</a>

You can use `echo` statement in functions to output the individual array values in the proper order, then the script must reassemble them into a new array variable, for example:     
```
#!/bin/bash

$ cat arrparams.sh 
#!/bin/bash
function testit {
  local thisarray=(1 2 3 4)
  echo ${thisarray[*]}
}
myarray=(`testit`)
echo "myarray is: ${myarray[*]}"

$ ./arrparams.sh 
myarray is: 1 2 3 4
```


## Function Recursion <a id="FR">[≡](#≡)</a>

One feature that local function variables provides is _self-containment_. A _self-contained_ function doesn’t use any resources outside of the function, other than whatever variables the script passes to it in the command line.

This feature enables the function to be called recursively. The classic example of a recursive algorithm is calculating factorials, A factorial of a number is the value of the preceding numbers multiplied with the number.    
5! = 1 * 2 * 3 * 4 * 5     
n! = (n-1)! * n        
```
#!/bin/bash

$ cat arrparams.sh 
#!/bin/bash
function calfactorial {
  if [ $1 -eq 1 ]; then
    echo 1
  else
    local temp=$[ $1 - 1 ]
    local result=`calfactorial $temp`
    echo $[ $result * $1]
  fi
}

read -p "Please enter the value: " value
result=`calfactorial $value`
echo "The factorial of $value is: $result"

$ ./arrparams.sh 
Please enter the value: 5
The factorial of 5 is: 120
```


## Creating a Library <a id="CAL">[≡](#≡)</a>

It’s easy to see how functions can help save typing in a single script, but how about if you just happen to use the same single code block between scripts?   

There’s a solution for that problem! The bash shell allows you to create a library file for your functions, then reference that single library file in as many scripts as you need to.   

The first step in the process is to create a common library file that contains the functions you need in your scripts. Here’s a simple library file called myfuncs that defines three simple functions:    
```
#!/bin/bash

$ cat myfuncs
#my script functions
function addem {
  echo $[ $1 + $2 ]
}

function mulem {
  echo $[ $1 * $2 ]
}

function divem {
  if [ $2 -ne 0 ]; then
    echo $[ $1 / $2 ]
  else
    echo -1
  fi
}
```

**_Notice_**: The library file need not to be executable.


The next step is to include the myfuncs library file in your script files that want to use any of the functions.

Just as with environment variables, shell functions are only valid for the shell session in which you define them. If you run the myfuncs shell script from your shell command line interface prompt, the shell creates a new shell, and runs the script in that new shell. This will define the three functions for that shell, but when you try to run  another script that uses those functions, they won’t be available, for example:     
```
#!/bin/bash

$ cat usefuncs
#!/bin/bash
./myfuncs
result=`addem 10 15`
echo "The result is $result"
$ ./usefuncs
./usefuncs: addem: command not found
The result is
```

The key to using function libraries is the source command. The source command executes
commands within the current shell context instead of creating a new shell to execute them. You use the source command to run the library file script inside of your shell script. This makes the functions available to the script. 

The source command has a shortcut alias, called the dot operator. To source the myfuncs library file in a shell script, all you need to do is add the following line:
```
. ./myfuncs
```

The whole example is:            
```
$ cat usefuncs 
#!/bin/bash
. ./myfuncs

val1=5
val2=10
val3=0
echo "addem $val1 $val2 = `addem $val1 $val2`"
echo "mulem $val1 $val2 = `mulem $val1 $val2`"
echo "divem $val2 $val1 = `divem $val2 $val1`"
echo "divem $val2 $val3 = `divem $val2 $val3`"

$ ./usefuncs 
addem 5 10 = 15
mulem 5 10 = 50
divem 10 5 = 2
divem 10 0 = -1
```