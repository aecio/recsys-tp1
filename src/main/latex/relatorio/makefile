file=tp01
build: bibtex compile clear

compile:
	pdflatex ${file}.tex
#	dvipdf ${file}.dvi
bibtex:
	pdflatex ${file}.tex
	bibtex ${file}
	pdflatex ${file}.tex
view:
	okular ${file}.pdf &
aspell:
	aspell --encoding="utf-8" -c -t=tex --lang="pt_BR" ${file}.tex
clear:
	rm -f ${file}.aux ${file}.blg ${file}.bbl ${file}.dvi ${file}.ps ${file}.log ${file}.toc ${file}.synctex.gz
