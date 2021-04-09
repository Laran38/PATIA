import matplotlib.pyplot as plt
import pandas as pd
import argparse
import os
import sys


def manage_options():
    parser = argparse.ArgumentParser()

    parser.add_argument('-i', '--input', dest='input', help='input file', type=str)
    parser.add_argument('-o', '--output', dest='output', help='output file', type=str)

    options = parser.parse_args()

    if options.input is None:
        parser.print_usage()
        sys.exit()

    return options


def generate_output_file(options):
    # filename for the output
    if options.output is None:
        prefix, ext = os.path.splitext(options.input)
        outname = prefix + '.pdf'
    else:
        outname = options.output
    return outname


def main():
    options = manage_options()
    output = generate_output_file(options)
    data = pd.read_csv(options.input, delimiter=';')

    data.sort_values(by=['SAT'], na_position='last')
    print(data)
    # Uses the first column for the x axes

    ax = data.plot(x=data.columns[0])

    # Set the bottom value to 0 for the Y axes
    ax.set_ylim(bottom=0)

    ax.set_xlabel('Nom du probleme', fontsize='x-large')
    ax.set_ylabel("Temps d'execution", fontsize='x-large')

    # setting font sizes
    ax.legend(fontsize='x-large')
    plt.yticks(fontsize='x-large')
    plt.xticks(fontsize='x-large')
    plt.savefig(output, format='pdf', dpi=1200)

    plt.show()

if __name__ == '__main__':
    main()

