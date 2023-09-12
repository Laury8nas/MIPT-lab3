package com.example.miptlab3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.miptlab3.databinding.ActivityMainBinding
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var canAddOperation = false
    private var canAddDecimal = true
    private var isEqual = false
    private var lastNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun numberAction(view: View) {
        if(view is Button) {

            if(view.text == ".") {
                if(canAddDecimal) {
                    binding.tvOperations.append(view.text)
                }
                canAddDecimal = false
            } else {
                binding.tvOperations.append(view.text)
            }
            canAddOperation = true
        }
    }

    fun operationAction(view: View) {
        if(view is Button && canAddOperation) {
            lastNumber = ""
            if(view.text != "+/-") {
                binding.tvOperations.append(view.text)
                canAddOperation = false
                canAddDecimal = true
            }
        }
    }

    fun rootAction(view: View) {

        val pattern = Regex("""-?\d+\.\d+|-?\d+""")
        val matches = pattern.findAll(binding.tvOperations.text.toString())
        val lastMatch = matches.last()

        lastNumber = lastMatch.value

        if(lastNumber.isNotEmpty() && lastNumber.toFloat() >= 0 && isEqual == false)  {
            val number: Float = lastNumber.toFloat()
            val regex = Regex("$lastNumber(?!.*$lastNumber)")
            val replacedString = binding.tvOperations.text.toString().replace(regex, sqrt(number).toString())
            binding.tvOperations.text = replacedString
            binding.tvResult.text = sqrt(number).toString()
            lastNumber = sqrt(number).toString()
        } else if(binding.tvResult.text.isNotEmpty() && binding.tvResult.text.toString().toFloat() >= 0 && isEqual == true) {
            val number: Float =  lastNumber.toFloat()
            val regex = Regex("$lastNumber(?!.*$lastNumber)")
            val replacedString = binding.tvOperations.text.toString().replace(regex, sqrt(number).toString())

            binding.tvOperations.text = replacedString
            binding.tvResult.text = sqrt(number).toString()
            lastNumber = sqrt(number).toString()
            isEqual = false
        } else if(lastNumber.toFloat() >= 0) {
            val number: Float =  lastNumber.toFloat()
            val regex = Regex("$lastNumber(?!.*$lastNumber)")
            val replacedString = binding.tvOperations.text.toString().replace(regex, sqrt(number).toString())

            binding.tvOperations.text = replacedString
            binding.tvResult.text = sqrt(number).toString()
            lastNumber = sqrt(number).toString()
        } else {
            binding.tvResult.text = "NaN"
        }
    }

    fun allClearAction(view: View) {
        binding.tvOperations.text = ""
        binding.tvResult.text = "0"
        lastNumber = ""
        canAddOperation = false
    }

    fun backSpaceAction(view: View) {
        val length = binding.tvOperations.text.toString().length

        if(length > 0) {
            binding.tvOperations.text = binding.tvOperations.text.subSequence(0, length-1)
        }
    }

    fun equalsAction(view: View) {
        if(binding.tvOperations.text.isNotEmpty()) {
            if(binding.tvOperations.text.toString()[0] == '+') {
                binding.tvOperations.text = binding.tvOperations.text.toString().substring(1)
            }
        }

        binding.tvOperations.text = binding.tvOperations.text.toString().replace(Regex("[/*]\\+"), { matchResult ->
            matchResult.value.replace("+", "")
        })

        binding.tvResult.text = calculateResults()
        isEqual = true
    }

    fun plusOrMinus(view: View) {

        if(canAddOperation == true)
        {
            val pattern = Regex("""\d+\.?\d*$""")
            val matches = pattern.findAll(binding.tvOperations.text.toString())
            val lastMatch = matches.last()

            lastNumber = lastMatch.value

            if (lastNumber.isNotEmpty()) {
                var number: Float = lastNumber.toFloat()
                val regex = Regex("([-+]?)$lastNumber(?!.*$lastNumber)")

                val operationsString = binding.tvOperations.text.toString()

                if (operationsString.isNotEmpty() && operationsString.last() in listOf('+', '-')) {
                } else {
                    val replacedString = operationsString.replace(regex) {
                        val sign = it.groups[1]?.value ?: ""
                        if (sign == "-") {
                            "+$lastNumber"
                        } else {
                            "-$lastNumber"
                        }
                    }

                    binding.tvOperations.text = replacedString

                    val pattern2 = Regex("""(?:[-+]?)\d+\.\d+|(?:[-+]?)\d+""")
                    val matches2 = pattern2.findAll(binding.tvOperations.text.toString())
                    val lastMatch2 = matches2.last()
                    var lastNumber2 = lastMatch2.value

                    binding.tvResult.text = lastNumber2.toString()
                }
            }
        }

    }

    private fun calculateResults(): String {
        val digitsOperators = digitsOperators()

        if(digitsOperators.isEmpty()) {
            return "0"
        }

        val timesDivision = timesDivisionCalculate(digitsOperators)

        if(timesDivision.isEmpty()) {
            return "0"
        }

        val result = addSubtractCalculate(timesDivision)

        return result.toString()
    }

    private fun digitsOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        var isNegative = false

        for (character in binding.tvOperations.text) {
            if (character == '-' && currentDigit.isEmpty()) {
                isNegative = true
            } else if (character.isDigit() || character == '.') {
                currentDigit += character
            } else {
                if (currentDigit.isNotEmpty()) {
                    list.add(if (isNegative) -currentDigit.toFloat() else currentDigit.toFloat())
                    currentDigit = ""
                    isNegative = false
                }
                list.add(character)
            }
        }

        if (currentDigit.isNotEmpty()) {
            list.add(if (isNegative) -currentDigit.toFloat() else currentDigit.toFloat())
        }

        isNegative = false

        return list
    }

    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList

        while(list.contains('*') || list.contains('/')) {
            list = calcTimesDiv(list)
        }

        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size

        for(i in passedList.indices) {
            if(passedList[i] is Char && i != passedList.lastIndex && i < restartIndex) {
                val operator = passedList[i]
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float

                when(operator) {
                    '*' ->
                    {
                        newList.add(prevDigit * nextDigit)
                        restartIndex = i + 1
                    }
                    '/' ->
                    {
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i + 1
                    }
                    else ->
                    {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }

            if(i > restartIndex) {
                newList.add(passedList[i])
            }

        }

        return newList
    }

    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        var result = passedList[0] as Float

        for(i in passedList.indices) {
            if(passedList[i] is Char && i != passedList.lastIndex) {
                val operator = passedList[i]
                val nextDigit = passedList[i+1] as Float

                if(operator == '+') {
                    result += nextDigit
                }
                if(operator == '-') {
                    result -= nextDigit
                }
            }
        }

        return result
    }
}