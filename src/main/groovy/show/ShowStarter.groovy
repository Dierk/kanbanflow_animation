package show

import groovy.swing.SwingBuilder
import static java.awt.BorderLayout.CENTER
import static javax.swing.WindowConstants.EXIT_ON_CLOSE
import kanban.Kanban
import static java.awt.BorderLayout.SOUTH

def config = [
        wip:        6,
        producers : 3,
        consumers : 3,
        moveTime:   500,
        produceTime: 2000 ,
        consumeTime: 2000 ,
        log:        false
]
def component = new ShowComponent(config)
def kanban = new Kanban(showComponent: component, *:config)
new SwingBuilder().edt {
    frame visible:true, pack:true, defaultCloseOperation: EXIT_ON_CLOSE, {
        panel (border: emptyBorder(20)) {
            borderLayout()
            widget component, preferredSize: [900, 500] , constraints: CENTER
            panel constraints:SOUTH, {
                slider id:'p', minimum:0, maximum:3000, value: kanban.produceTime, stateChanged: { kanban.produceTime = p.value }
                button "+", actionPerformed: {kanban.plusWip()}
                slider id:'m', minimum:0, maximum:1000, value: kanban.moveTime, stateChanged: { kanban.moveTime = m.value }
                button "-", actionPerformed: {kanban.minusWip()}
                slider id:'c', minimum:0, maximum:3000, value: kanban.consumeTime, stateChanged: { kanban.consumeTime = c.value }
            }
        }
    }
}
kanban.run()
