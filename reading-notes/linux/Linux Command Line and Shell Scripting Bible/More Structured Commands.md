[Back](index.md)

# Reading Notes ~ More Structured Commands

## Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 9: More Structured Commands.md
- Pages: {259, }
- Reading Time: 31/03/2017 07:47 ~ 

## Content Navigation <a id="≡"></a>
- [The for Command](#TFC)
    + [Reading values in a list](#TFC-RVIL)
    + [Reading complex values in a list](#TFC-RCVIL)
    + [Reading a list from a variable](#TFC-RVFV)
    + [Reading values from a command](#TFC-RVFC)

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