#Listar användningsområden för Pubsubhubbub

# Pubsubhubbub och indexering #
Pubsubhubbub kan istället med fördel användas för att driva indexering av dokument. Sökmotorn får då vara en server som lyssnar på uppdateringar från flöden. Källsystemen implementerar signalering till pubsubhubbub vilket gör att dokumenten blir indexerade väldigt snabbt och sökmotorn slipper crawla in dokument. Detta innebär att lasten på källsystemen (framförallt EpiServer) kommer minska betydligt.

Fördelen är också att ett protokoll kan användas både för att göra dokument sökbara men också för att signalera till andra system.