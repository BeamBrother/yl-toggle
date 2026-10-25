import React, { ReactElement, ReactNode, SyntheticEvent } from 'react';
import { EventTrack } from 'utils/track';

interface ChildProps {
  onClick?: (event: SyntheticEvent) => void;
  children?: ReactNode;
}

interface IProps {
  category: string;
  action: string;
  children: ReactElement<ChildProps>;
}

const EventTracker = (props: IProps) => {
  const { category, action, children } = props;
  const child = React.Children.only(children) as ReactElement<ChildProps>;

  const onClick = (event: SyntheticEvent) => {
    EventTrack.track(category, action);
    child.props.onClick?.(event);
  };

  return React.cloneElement<ChildProps>(child, { ...child.props, onClick }, child.props.children);
};

export default EventTracker;
