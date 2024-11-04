# Warmup

We have defined a function f:

```
f(x) = f(x-1) + f(x-1)
f(0) = 1
```

In Scala we can define this function as:

```scala
def f(x: Int): Int = x match
  case 0 => 1
  case x => f(x - 1) + f(x - 1)
```

Note: this function implementation does not account for the case when x is negative.
Passing a negative number to this function will result in infinite recursion and stack overflow.
To avoid this we can add a case for negative numbers and use for example the `Either` type as a return type
or throw an `IllegalArgumentException`. In this case, we will assume correctness of the passed argument.

To calculate the value of f(x) we need to calculate f(x-1) twice.
Conversely, each invocation of f(x-1) calculates f(x-2) twice, and so on until we reach f(0).
Since the number of invocations of function f doubles at each level
and to calculate f(0) we need to perform exactly one calculation,
the number invocation of this function for x is equal to $2^x$.
Therefore, time complexity of f(x) is O($2^x$).

Since f(x-1) + f(x-1) = 2f(x-1), we can rewrite this function as:

```
f(x) = 2f(x-1)
f(0) = 1
```

In Scala we can define this function as:

```scala
def f(x: Int): Int = x match
  case 0 => 1
  case x => 2 * f(x - 1)
```

Now we calculate f(x-1) once at each level until we reach f(0).
Time complexity of this solution is O(n).

Since f(x) = $2^x$, we can further reduce its complexity.
One way to do this is to use binary exponentiation.
An important observation is that $n^x$ = $(n^{x/2})^2$.
We can use this observation to rewrite the function as:

```
f(x) = 2f(x-1) if x is odd
f(x) = (f(x/2))^2 if x is even
f(0) = 1
```

In Scala, we can define this function as:

```scala
def f(x: Int): Int = x match
  case 0 => 1
  case x => x % 2 match
    case 1 => 2 * f(x - 1)
    case 0 =>
      val tmp = f(x / 2)
      tmp * tmp
```

Since we divide x by 2 at least every other time, the time complexity of this solution is O(log(n)).

Since this is not any exponentiation function, but $2^x$, we can also make use of bitwise shift operation.
Shifting a number by x bits to the left is equivalent to multiplying it by $2^x$.
Therefore, to obtain $2^x$, we can shift 1 by x bits to the left.

We can implement this in a one line of Scala code:

```scala
def f(x: Int): Int = 1 << x
```

Bitwise shifting is a constant time operation, which means that the time complexity of this function is O(1).
