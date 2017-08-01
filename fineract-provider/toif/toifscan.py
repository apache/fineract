import subprocess
import os
import shlex
import sys

buildDir = sys.argv[1]
HOUSE_KEEPING = "housekeeping/housekeeping.txt"
OUTPUT_DIR = "../build/reports/toif"
SUB_PROCESS = []

def prepareAdaptors(inFilePath):

    commonArgs = ["--housekeeping", HOUSE_KEEPING, "--outputdirectory", OUTPUT_DIR, "--inputfile", inFilePath]

    fb_cmd = ["toif", "--adaptor","Findbugs"]
    fb_cmd.extend( commonArgs )
    print fb_cmd
    p = subprocess.Popen( fb_cmd, shell=False)
    SUB_PROCESS.append( p )

    jl_cmd = ["toif", "--adaptor", "Jlint"]
    jl_cmd.extend(commonArgs)
    print jl_cmd
    p = subprocess.Popen(jl_cmd, shell=False)
    SUB_PROCESS.append(p)

def scanBuildDir():
    for root, dirnames, filenames in os.walk(buildDir):
        for inFile in filenames:
            if inFile.endswith(".class"):
                inFilePath = os.path.join(root, inFile)
                print 'infile is ', inFilePath
                prepareAdaptors(inFilePath)
                for p in SUB_PROCESS:
                    p.wait()

if __name__ == '__main__':
    scanBuildDir()