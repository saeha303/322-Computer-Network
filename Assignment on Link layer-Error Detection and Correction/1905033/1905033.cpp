#include <iostream>
#include <string>
#include <cstring>
#include <bitset>
#include <vector>
#include <cmath>
#include <iomanip>
#include <Windows.h>
#include <random>
#include <algorithm>
using namespace std;

// Function to perform modulo 2 division on binary numbers
string modulo2Division(string dividend, string divisor)
{
    // string quotient = ""; // Initialize quotient as an empty string

    while (dividend.length() >= divisor.length())
    {
        // Find the leftmost '1' bit in the dividend
        // size_t shift = dividend.length() - divisor.length();
        while (dividend[0] == '0')
        {                                  // && shift > 0
            dividend = dividend.substr(1); // Shift the dividend to the right
            // shift--;
        }
        if (dividend.length() < divisor.length())
        {
            break;
        }
        // XOR the dividend with the shifted divisor
        for (size_t i = 0; i < divisor.length(); i++)
        {
            if (dividend[i] == divisor[i])
            {
                dividend[i] = '0';
            }
            else
            {
                dividend[i] = '1';
            }
        }
        // Append the result (0 or 1) to the quotient
        // quotient += dividend[0];

        // Remove the leftmost bit (already used in the XOR)
        // dividend = dividend.substr(1);
    }

    // return quotient;
    return dividend;
}
int check_bit(int m)
{
    int r = 0;
    while (m > pow(2, r) - r)
    {
        r++;
    }
    return r;
}
void crc_table(int m, int n, int **arr)
{
    for (int j = 0; j < n; j++)
    {
        for (int i = 0; i < m; i++)
        {
            int value = j + 1; // Calculate the value to check
            if ((value & (1 << i)))
            {
                // Check if (j+1) can be expressed as a sum of 2's power within the range i
                arr[i][j] = 1;
            }
        }
    }
    // for (int i = 0; i < m; i++)
    // {
    //     for (int j = 0; j < n; j++)
    //     {
    //         cout<<arr[i][j]<<' ';
    //     }
    //     cout<<endl;
    // }
}
unsigned int pow_of_2(unsigned int result, unsigned int pow)
{
    return (result & (1 << pow)) == result;
}
void fill_check_bit(vector<vector<unsigned long>> &vct, int m, int n, int **arr, int row, int col)
{
    unsigned int xor_sum = 0;
    for (int i = 0; i < col; i++)
    {
        if (arr[row][i] == 1 && vct[m][i] == 1)
        {
            xor_sum++;
        }
    }
    if (xor_sum % 2)
    {
        vct[m][n] = 1;
    }
    else
    {
        vct[m][n] = 0;
    }
}
int error_correction(vector<vector<unsigned long>> &vct_wth_check, int **crc_tab, int row, int row_len, int noOfCheckBits, int numRows)
{
    unsigned int xor_sum = 0;
    int pow_sum = 0;
    for (int i = 0; i < noOfCheckBits; i++)
    {
        for (int j = 0; j < row_len; j++)
        {
            if (crc_tab[i][j] == 1 && vct_wth_check[row][j] == 1)
            {
                xor_sum++;
            }
        }
        if (xor_sum % 2)
        {
            pow_sum += pow(2, i);
        }
        xor_sum = 0;
    }

    // vct_wth_check[row][pow_sum-1]=abs(1-vct_wth_check[row][pow_sum-1]);
    // if(vct_wth_check[row][pow_sum-1]){
    //     vct_wth_check[row][pow_sum-1]=0;
    // }else{
    //     vct_wth_check[row][pow_sum-1]=1;
    // }
    return pow_sum - 1;
}
int main()
{
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    string dataStr, temp, gp;
    int m;
    double p;
    cout << "enter data string: ";
    getline(cin, dataStr);
    // cout << dataStr.length() << '\n';
    cout << "enter number of data bytes in a row <m>: ";
    cin >> m;
    cout << "enter probability <p>: ";
    cin >> p;
    getline(cin, temp);
    cout << "enter generator polynomial: ";
    getline(cin, gp);
    int paddingSize;
    if (dataStr.length() % m)
        paddingSize = m - (dataStr.length() % m);
    // Append padding characters (~) to make the size a multiple of m
    for (int i = 0; i < paddingSize; i++)
    {
        dataStr += '~';
    }
    cout<<endl;
    cout << "data string after padding: " << dataStr << '\n';
    int dataSize = dataStr.length();
    int numRows = dataSize / m; // Calculate the number of rows needed
    // vector<vector<string>> vct(numRows, vector<string>(m * 8, ""));
    vector<string> vct;
    string str = "";
    cout<<endl;
    cout << "data block <ascii code of m characters per row>" << endl;
    for (int row = 0; row < numRows; row++)
    {
        int startIdx = row * m;
        int endIdx = min((row + 1) * m, dataSize); // Handle the last row with fewer characters
        // cout << "Row " << (row + 1) << ": ";

        for (int i = startIdx; i < endIdx; i++)
        {
            // Convert the ASCII code to 8-bit binary representation
            bitset<8> binary(dataStr[i]);
            str += binary.to_string();
            // for (int j = 0; j < 8; j++)
            // {
            //     str += binary[j] ? "1" : "0";
            //     cout<<binary[j]<<' ';
            //     // vct[row].push_back(binary[j] ? "1" : "0");
            //     // cout<<vct[row].back();
            // }
            // cout<<endl;
            // cout<<"binary"<<'\n';
            cout << binary << "";
        }
        vct.push_back(str);
        // cout<<vct.back();
        str = "";
        cout << endl;
    }
    vct.resize(dataStr.length() * 8);
    // for (int row = 0; row < numRows; row++)
    // {
    // for (int i = 0; i < vct.size(); i++)
    // {
    //     cout << vct[i];
    // }
    // cout << endl;
    // }
    cout<<endl;
    cout << "data block after adding check bits:" << endl;
    int noOfCheckBits = check_bit(m * 8 + 1);
    int row_len = m * 8 + noOfCheckBits;
    int **crc_tab = new int *[noOfCheckBits];
    for (int i = 0; i < noOfCheckBits; i++)
    {
        crc_tab[i] = new int[row_len];
    }
    for (int i = 0; i < noOfCheckBits; i++)
    {
        for (int j = 0; j < row_len; j++)
        {
            crc_tab[i][j] = 0;
        }
    }
    crc_table(noOfCheckBits, row_len, crc_tab);
    vector<vector<unsigned long>> vct_wth_check(numRows, vector<unsigned long>(row_len, 0));
    unsigned int j = 0;
    int pointer = 0;
    for (int row = 0; row < numRows; row++)
    {
        for (unsigned int i = 0; i < row_len; i++)
        {
            if (pow_of_2(i + 1, j))
            {
                vct_wth_check[row][i] = 0;
                j++;
            }
            else
            {
                if (vct[pointer].front() == '1')
                {
                    vct_wth_check[row][i] = 1;
                }
                else if (vct[pointer].front() == '0')
                {
                    vct_wth_check[row][i] = 0;
                }
                vct[pointer] = vct[pointer].substr(1);
            }
        }
        j = 0;
        pointer++;
    }
    j = 0;
    pointer = 0;
    for (int row = 0; row < numRows; row++)
    {
        for (unsigned int i = 0; i < row_len; i++)
        {
            if (pow_of_2(i + 1, j))
            {
                // cout << "case 1 " << i << '\n';
                fill_check_bit(vct_wth_check, row, i, crc_tab, j, row_len);
                j++;
            }
        }
        j = 0;
        pointer++;
    }
    j = 0;
    for (int row = 0; row < numRows; row++)
    {
        for (int i = 0; i < row_len; i++)
        {
            if (pow_of_2(i + 1, j))
            {
                SetConsoleTextAttribute(hConsole, 10);
                cout << vct_wth_check[row][i];
                j++;
            }
            else
            {
                SetConsoleTextAttribute(hConsole, 15);
                cout << vct_wth_check[row][i];
            }
        }
        j = 0;
        cout << endl;
    }
    SetConsoleTextAttribute(hConsole, 15);
    cout<<endl;
    cout << "data bits after column wise serialization:" << '\n';
    string destr = "";
    for (int i = 0; i < row_len; i++)
    {
        for (int row = 0; row < numRows; row++)
        {
            destr += to_string(vct_wth_check[row][i]);
            // cout<<vct_wth_check[row][i]<<'\n';
        }
    }
    cout << destr << '\n';
    temp = destr;
    int shift = gp.length() - 1;
    for (int i = 0; i < shift; i++)
    {
        destr += "0";
    }
    string dividend = destr; // Example dividend
    string divisor = gp;     // Example divisor

    string result = modulo2Division(dividend, divisor);

    while (result.length() < gp.length() - 1)
    {
        result = "0" + result;
    }
    cout<<endl;
    cout << "data bits after sending CRC checksum <sent frame>: " << '\n';
    cout << temp;
    SetConsoleTextAttribute(hConsole, 11);
    cout << result << endl;
    SetConsoleTextAttribute(hConsole, 15);
    int sent_len = temp.length() + result.length();
    // cout<<sent_len<<'\n';
    // cout<<p<<'\n';
    int wrong_bit = (int)(p * sent_len);
    // cout<<wrong_bit<<'\n';
    string sent = temp + result;
    string sent_temp = sent;
    random_device rd;
    mt19937 gen(rd());
    // uniform_int_distribution<> dis(0, sent.length() - 1);
    uniform_real_distribution<> dis(0.0, 1.0);
    // Simulate the physical transmission by toggling each bit with probability p
    vector<int> arr(sent_len, 0);
    // for (int i = 0; i < wrong_bit; i++)
    // {
    //     int randomValue = dis(gen); // Generate a random value between 0 and 1
    //     arr[randomValue] = 1;
    //     // if (randomValue < p) {
    //     if (sent[randomValue] == '1')
    //     {
    //         sent[randomValue] = '0';
    //     }
    //     else
    //     {
    //         sent[randomValue] = '1';
    //     }
    // }
    for (int i = 0; i < sent_len; i++)
    {
        double randomValue = dis(gen); // Generate a random value between 0 and 1
        if (randomValue < p)
        {
            // bitStream.flip(i); // Toggle the bit if the random value is less than p
            arr[i] = 1;
            if (sent[i] == '1')
            {
                sent[i] = '0';
            }
            else
            {
                sent[i] = '1';
            }
        }
    }
    // sort(arr.begin(),arr.end());
    cout<<endl;
    cout << "received frame: " << '\n';
    for (int i = 0; i < sent_len; i++)
    {
        if (arr[i])
        {
            SetConsoleTextAttribute(hConsole, 12);
            cout << sent[i];
        }
        else
        {
            SetConsoleTextAttribute(hConsole, 15);
            cout << sent[i];
        }
    }
    cout << '\n';
    SetConsoleTextAttribute(hConsole, 15);
    result = "";
    result = modulo2Division(sent, divisor);
    cout<<endl;
    // cout<<"ouhfousfgo"<<'\n';
    // cout<<result<<'\n';
    if (result != "")
    {
        cout << "result of CRC checksum matching: error detected" << '\n';
    }
    else
    {
        cout << "result of CRC checksum matching: no error detected" << '\n';
    }
    sent = sent.substr(0, sent.length() - gp.length() + 1);
    // cout<<sent<<'\n';
    for (int j = 0; j < row_len; j++)
    {
        for (int i = 0; i < numRows; i++)
        {
            if (sent[j * numRows + i] == '1')
                vct_wth_check[i][j] = 1;
            else
                vct_wth_check[i][j] = 0;
        }
    }
    j = 0;
    cout<<endl;
    cout << "data block after removing CRC checksum bits:" << '\n';
    for (int row = 0; row < numRows; row++)
    {
        for (int i = 0; i < row_len; i++)
        {
            if (arr[i * numRows + row])
            {
                // cout<<"fvhoe:"<<row*row_len+i<<'\n';
                SetConsoleTextAttribute(hConsole, 12);
                cout << vct_wth_check[row][i];
                // j++;
            }
            else
            {
                SetConsoleTextAttribute(hConsole, 15);
                cout << vct_wth_check[row][i];
            }
        }
        j = 0;
        cout << endl;
    }
    SetConsoleTextAttribute(hConsole, 15);
    j = 0;
    cout<<endl;
    cout << "data block after removing check bits:" << '\n';
    string bitStream = ""; // Example bitstream
    for (int row = 0; row < numRows; row++)
    {
        int err = error_correction(vct_wth_check, crc_tab, row, row_len, noOfCheckBits, numRows);
        if (err != -1)
        {
            if (vct_wth_check[row][err])
            {
                vct_wth_check[row][err] = 0;
            }
            else
            {
                vct_wth_check[row][err] = 1;
            }
        }
        for (int i = 0; i < row_len; i++)
        {
            if (pow_of_2(i + 1, j))
                j++;
            else
            {
                cout << vct_wth_check[row][i];
                bitStream += to_string(vct_wth_check[row][i]);
            }
        }
        j = 0;
        cout << endl;
    }
    for (int i = 0; i < noOfCheckBits; i++)
    {
        delete[] crc_tab[i];
    }
    delete[] crc_tab;

    // Ensure the bitstream length is a multiple of 8
    if (bitStream.length() % 8 != 0)
    {
        cerr << "Invalid bitstream length." << endl;
        return 1;
    }

    string asciiString;

    for (size_t i = 0; i < bitStream.length(); i += 8)
    {
        string binarySegment = bitStream.substr(i, 8);    // Get 8-bit segment
        int asciiValue = stoi(binarySegment, nullptr, 2); // Convert binary to integer
        char asciiChar = static_cast<char>(asciiValue);   // Convert integer to ASCII character
        asciiString += asciiChar;                         // Append to the result string
    }
    cout<<endl;
    cout << "output frame: " << asciiString << endl;
    return 0;
}
