package edu.fdu.se.astdiff.miningactions;

import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.astdiff.miningactions.Body.*;
import edu.fdu.se.astdiff.miningactions.bean.MiningActionData;
import edu.fdu.se.astdiff.miningactions.statement.*;
import edu.fdu.se.astdiff.miningactions.util.AstRelations;
import edu.fdu.se.astdiff.miningactions.util.StatementConstants;


import java.util.List;

/**
 * Created by huangkaifeng on 2018/1/23.
 *
 */
public class ClusterInsertOrDelete {

    private List<Action> actionList;
    private MiningActionData fp;
    private Class clazz;

    public ClusterInsertOrDelete(Class mClazz, MiningActionData mminingActionData) {
        this.fp = mminingActionData;
        this.clazz = mClazz;
        if (Insert.class.equals(clazz)) {
            this.actionList = mminingActionData.mGeneratingActionsData.getInsertActions();
        } else if (Delete.class.equals(clazz)) {
            this.actionList = mminingActionData.mGeneratingActionsData.getDeleteActions();
        } else if (Move.class.equals(clazz)) {
            this.actionList = mminingActionData.mGeneratingActionsData.getMoveActions();
        } else {
            this.actionList = mminingActionData.mGeneratingActionsData.getUpdateActions();
        }
    }


    public void doCluster() {
        int insertActionCount = this.actionList.size();
        int insertActionIndex = 0;
        while (insertActionIndex != insertActionCount) {
            Action a = this.actionList.get(insertActionIndex);
            insertActionIndex++;
            if (fp.mGeneratingActionsData.getAllActionMap().get(a) == 1) {
                // 标记过的action
                continue;
            }
            String curAction = ActionPrinter.getMyOneActionString(a, 0, fp.mSrcTree);
            Tree insNode = (Tree) a.getNode();
            String type = insNode.getAstClass().getSimpleName();
            Tree fafafather = AstRelations.findFafafatherNode(insNode);
            if (fafafather == null) {
                System.err.println("Father Null Condition: " + a.getClass().getSimpleName() + " " + type);
                fp.setActionTraversedMap(a);
                continue;
            }
            String fatherType = fafafather.getAstClass().getSimpleName();

            boolean isBody = true;
            switch (type) {
                case StatementConstants.TYPEDECLARATION:
                    MatchClass.matchClassSignature(fp,a,fafafather);
                    break;
                case StatementConstants.FIELDDECLARATION:
                    MatchFieldDeclaration.matchFieldDeclaration(fp, a);break;
                case StatementConstants.INITIALIZER:
                    MatchInitializerBlock.matchInitializerBlock(fp, a, type);break;
                case StatementConstants.METHODDECLARATION:
                    MatchMethod.matchNewOrDeleteMethod(fp, a);break;
                default:
                    isBody = false;
            }
            if (isBody)
                continue;
            boolean isSignature = true;

            switch (fatherType) {
                case StatementConstants.TYPEDECLARATION:
                    MatchFieldDeclaration.matchFieldDeclarationByFather(fp, a, type, fafafather, fatherType);break;
                case StatementConstants.FIELDDECLARATION:
                    MatchFieldDeclaration.matchFieldDeclarationByFather(fp, a, type, fafafather, fatherType);break;
                case StatementConstants.INITIALIZER:break;
                case StatementConstants.METHODDECLARATION:
                    MatchMethod.matchMethodSignatureChange(fp, a, type, fafafather, fatherType);break;
                default:
                    isSignature = false;
            }
            if (isSignature)
                continue;

            // BodyDeclaration body 内部
            switch (type) {
                case StatementConstants.IFSTATEMENT:
                    MatchIfElse.matchIf(fp, a ,type);
                    break;
                case StatementConstants.BLOCK:
                    MatchBlock.matchBlock(fp, a ,type);
                    break;
                case StatementConstants.BREAKSTATEMENT:
                    if (AstRelations.isFatherXXXStatement(a, StatementConstants.SWITCHSTATEMENT)) {
                        //增加switch语句
                        MatchSwitch.matchSwitchCaseByFather(fp, a, fafafather);
                    } else {
                        System.out.println("Other Condition" + a.getClass().getSimpleName() + " " + type);
                        fp.setActionTraversedMap(a);
                    }
                    break;
                case StatementConstants.RETURNSTATEMENT:
                    MatchReturnStatement.matchReturnStatement(fp, a, type);
                    break;
                case StatementConstants.FORSTATEMENT:
                    //增加for语句
                    MatchForStatement.matchForStatement(fp, a, type);
                    break;
                case StatementConstants.ENHANCEDFORSTATEMENT:
                    //增加for语句
                    MatchForStatement.matchEnhancedForStatement(fp, a, type);
                    break;
                case StatementConstants.WHILESTATEMENT:
                    //增加while语句
                    MatchWhileStatement.matchWhileStatement(fp, a, type);
                    break;
                case StatementConstants.DOSTATEMENT:
                    //增加do while语句
                    MatchWhileStatement.matchDoStatement(fp, a, type);
                    break;
                case StatementConstants.TRYSTATEMENT:
                    MatchTry.matchTry(fp, a, type);
                    break;
                case StatementConstants.THROWSTATEMENT:
                    MatchTry.matchThrowStatement(fp, a, type, fafafather, fatherType);
                    break;
                case StatementConstants.VARIABLEDECLARATIONSTATEMENT:
                    MatchVariableDeclarationExpression.matchVariableDeclaration(fp, a, type);
                    break;
                case StatementConstants.EXPRESSIONSTATEMENT:
                    if (AstRelations.isFatherXXXStatement(a, StatementConstants.IFSTATEMENT) && a.getNode().getParent().getChildPosition(a.getNode()) == 2) {
                        // Pattenr 1.2 Match else
                        MatchIfElse.matchElse(fp, a, type, fafafather, fatherType);
                    } else {
                        MatchExpressionStatement.matchExpression(fp, a);
                    }
                    break;
//                case StatementConstants.CONDITIONALEXPRESSION:
//                    MatchConditionalExpression.matchConditionalExpression(fp, a, type);
//                    break;
                case StatementConstants.SYNCHRONIZEDSTATEMENT:
                    //同步语句块增加
                    MatchSynchronized.matchSynchronized(fp, a, type);
                    break;
                case StatementConstants.SWITCHSTATEMENT:
                    //增加switch语句
                    MatchSwitch.matchSwitch(fp, a, type);
                    break;
                case StatementConstants.SWITCHCASE:
                    //增加switch语句
                    MatchSwitch.matchSwitchCase(fp, a, type);
                    break;
                case StatementConstants.JAVADOC:
                    //增加javadoc
                    MatchJavaDoc.matchJavaDoc(fp, a, type);
                    break;
                case StatementConstants.CONSTRUCTORINVOCATION:
                    //构造方法this
                    MatchMethod.matchConstructorInvocation(fp, a);
                    break;
                case StatementConstants.SUPERCONSTRUCTORINVOCATION:
                    //构造方法super
                    MatchMethod.matchSuperConstructorInvocation(fp, a, type);
                    break;
                case StatementConstants.TAGELEMENT:
                case StatementConstants.TEXTELEMENT:
                case StatementConstants.SIMPLENAME:
                case StatementConstants.SIMPLETYPE:
                case StatementConstants.STRINGLITERAL:
                case StatementConstants.NULLLITERAL:
                case StatementConstants.PREFIXEXPRESSION:
                case StatementConstants.CHARACTERLITERAL:
                case StatementConstants.NUMBERLITERAL:
                case StatementConstants.BOOLEANLITERAL:
                case StatementConstants.INFIXEXPRESSION:
                case StatementConstants.METHODINVOCATION:
                case StatementConstants.QUALIFIEDNAME:
                case StatementConstants.MODIFIER:
                case StatementConstants.MARKERANNOTATION:
                case StatementConstants.NORMALANNOTATION:
                case StatementConstants.SINGLEMEMBERANNOTATION:
                case StatementConstants.ASSIGNMENT:
                    MatchSimpleNameOrLiteral.matchSimplenameOrLiteral(fp, a, type);
                    break;
                default:
//                    String operationEntity = "DEFAULT: " + a.getClass().getSimpleName() + " " + type;
//                    Range nodeLinePosition = AstRelations.getRangeOfAstNode(a);
//                    new ClusteredActionBean(a, type, null, nodeLinePosition, -1, operationEntity, fafafather, fatherType);
//                    fp.setActionTraversedMap(a);
                    break;
            }
        }
    }


    public void doClusterUpdate() {
        int updateActionCount = this.actionList.size();
        int index = 0;
        while (index != updateActionCount) {
            Action a = this.actionList.get(index);
            index++;
            if (fp.mGeneratingActionsData.getAllActionMap().get(a) == 1) {
                continue;
            }
            Tree tmp = (Tree) a.getNode();
            String type = tmp.getAstClass().getSimpleName();
            Tree fafafather = AstRelations.findFafafatherNode(tmp);
            if (fafafather == null) {
                System.out.println("Father Null Condition: " + a.getClass().getSimpleName() + " " + type);
                fp.setActionTraversedMap(a);
                continue;
            }
            switch (type) {
                case StatementConstants.TAGELEMENT:
                case StatementConstants.TEXTELEMENT:
                case StatementConstants.SIMPLENAME:
                case StatementConstants.SIMPLETYPE:
                case StatementConstants.STRINGLITERAL:
                case StatementConstants.NULLLITERAL:
                case StatementConstants.CHARACTERLITERAL:
                case StatementConstants.NUMBERLITERAL:
                case StatementConstants.BOOLEANLITERAL:
                case StatementConstants.INFIXEXPRESSION:
                case StatementConstants.METHODINVOCATION:
                case StatementConstants.QUALIFIEDNAME:
                case StatementConstants.MODIFIER:
                case StatementConstants.MARKERANNOTATION:
                case StatementConstants.NORMALANNOTATION:
                case StatementConstants.SINGLEMEMBERANNOTATION:
                    MatchSimpleNameOrLiteral.matchSimplenameOrLiteral(fp, a ,fafafather);
                    break;
                default:
                    break;
            }
        }
    }

}
